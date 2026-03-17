package com.rbih.loanevaluator.service;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanApplicationRequest;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.dto.response.LoanApplicationApprovedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationRejectedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationResponse;
import com.rbih.loanevaluator.dto.response.OfferDto;
import com.rbih.loanevaluator.enums.ApplicationStatus;
import com.rbih.loanevaluator.enums.RejectionReason;
import com.rbih.loanevaluator.enums.RiskBand;
import com.rbih.loanevaluator.model.LoanApplication;
import com.rbih.loanevaluator.repository.LoanApplicationRepository;
import com.rbih.loanevaluator.service.engine.InterestRateEngine;
import com.rbih.loanevaluator.service.engine.OfferGenerator;
import com.rbih.loanevaluator.service.engine.RiskClassifier;
import com.rbih.loanevaluator.service.rule.EligibilityEvaluator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanApplicationService {

    private final EligibilityEvaluator eligibilityEvaluator;
    private final RiskClassifier riskClassifier;
    private final InterestRateEngine interestRateEngine;
    private final OfferGenerator offerGenerator;
    private final LoanApplicationRepository repository;

    public LoanApplicationService(EligibilityEvaluator eligibilityEvaluator,
                                  RiskClassifier riskClassifier,
                                  InterestRateEngine interestRateEngine,
                                  OfferGenerator offerGenerator,
                                  LoanApplicationRepository repository) {
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.riskClassifier = riskClassifier;
        this.interestRateEngine = interestRateEngine;
        this.offerGenerator = offerGenerator;
        this.repository = repository;
    }

    public LoanApplicationResponse evaluate(LoanApplicationRequest request) {
        ApplicantDto applicant = request.getApplicant();
        LoanDto loan = request.getLoan();
        UUID applicationId = UUID.randomUUID();

        // Step 1: Run all eligibility rules
        List<RejectionReason> rejectionReasons = new ArrayList<>(
                eligibilityEvaluator.evaluate(applicant, loan)
        );

        // If any eligibility rule fails, reject immediately
        if (!rejectionReasons.isEmpty()) {
            List<String> reasonStrings = rejectionReasons.stream()
                    .map(Enum::name)
                    .toList();

            persistDecision(applicationId, applicant, loan,
                    ApplicationStatus.REJECTED, null, null, null, null, reasonStrings);

            return LoanApplicationRejectedResponse.builder()
                    .applicationId(applicationId)
                    .rejectionReasons(reasonStrings)
                    .build();
        }

        // Step 2: Classify risk band
        RiskBand riskBand = riskClassifier.classify(applicant.getCreditScore());

        // Step 3: Calculate final interest rate
        BigDecimal finalRate = interestRateEngine.calculateRate(applicant, loan, riskBand);

        // Step 4: Generate offer (50% threshold check)
        Optional<OfferDto> offer = offerGenerator.generate(applicant, loan, finalRate);

        if (offer.isEmpty()) {
            List<String> reasonStrings = List.of(RejectionReason.EMI_EXCEEDS_50_PERCENT_FOR_OFFER.name());

            persistDecision(applicationId, applicant, loan,
                    ApplicationStatus.REJECTED, null, finalRate, null, null, reasonStrings);

            return LoanApplicationRejectedResponse.builder()
                    .applicationId(applicationId)
                    .rejectionReasons(reasonStrings)
                    .build();
        }

        // Step 5: Approved
        OfferDto offerDto = offer.get();

        persistDecision(applicationId, applicant, loan,
                ApplicationStatus.APPROVED, riskBand, finalRate, offerDto.getEmi(), offerDto.getTotalPayable(), null);

        return LoanApplicationApprovedResponse.builder()
                .applicationId(applicationId)
                .riskBand(riskBand)
                .offer(offerDto)
                .build();
    }

    private void persistDecision(UUID id, ApplicantDto applicant, LoanDto loan,
                                 ApplicationStatus status, RiskBand riskBand,
                                 BigDecimal interestRate, BigDecimal emi, BigDecimal totalPayable,
                                 List<String> rejectionReasons) {
        LoanApplication entity = LoanApplication.builder()
                .id(id)
                .applicantName(applicant.getName())
                .applicantAge(applicant.getAge())
                .monthlyIncome(BigDecimal.valueOf(applicant.getMonthlyIncome()))
                .employmentType(applicant.getEmploymentType())
                .creditScore(applicant.getCreditScore())
                .loanAmount(BigDecimal.valueOf(loan.getAmount()))
                .tenureMonths(loan.getTenureMonths())
                .loanPurpose(loan.getPurpose())
                .status(status)
                .riskBand(riskBand)
                .interestRate(interestRate)
                .emi(emi)
                .totalPayable(totalPayable)
                .rejectionReasons(rejectionReasons != null ? String.join(",", rejectionReasons) : null)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(entity);
    }
}
