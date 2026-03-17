package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RejectionReason;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2)
public class AgeTenureRule implements EligibilityRule {

    private static final int MAX_AGE_PLUS_TENURE = 65;

    @Override
    public Optional<RejectionReason> evaluate(ApplicantDto applicant, LoanDto loan) {
        double tenureInYears = loan.getTenureMonths() / 12.0;
        double agePlusTenure = applicant.getAge() + tenureInYears;

        if (agePlusTenure > MAX_AGE_PLUS_TENURE) {
            return Optional.of(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);
        }
        return Optional.empty();
    }
}
