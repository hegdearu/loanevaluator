package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.dto.response.OfferDto;
import com.rbih.loanevaluator.service.calculator.EMICalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
public class OfferGenerator {

    private static final BigDecimal FIFTY_PERCENT = new BigDecimal("0.50");

    private final EMICalculator emiCalculator;

    public OfferGenerator(EMICalculator emiCalculator) {
        this.emiCalculator = emiCalculator;
    }

    public Optional<OfferDto> generate(ApplicantDto applicant, LoanDto loan, BigDecimal interestRate) {
        BigDecimal principal = BigDecimal.valueOf(loan.getAmount());
        BigDecimal emi = emiCalculator.calculate(principal, interestRate, loan.getTenureMonths());
        BigDecimal monthlyIncome = BigDecimal.valueOf(applicant.getMonthlyIncome());
        BigDecimal maxAllowedEmi = monthlyIncome.multiply(FIFTY_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        if (emi.compareTo(maxAllowedEmi) > 0) {
            return Optional.empty();
        }

        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, loan.getTenureMonths());

        return Optional.of(new OfferDto(interestRate, loan.getTenureMonths(), emi, totalPayable));
    }
}
