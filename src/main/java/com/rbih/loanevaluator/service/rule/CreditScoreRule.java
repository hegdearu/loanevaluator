package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RejectionReason;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class CreditScoreRule implements EligibilityRule {

    private static final int MINIMUM_CREDIT_SCORE = 600;

    @Override
    public Optional<RejectionReason> evaluate(ApplicantDto applicant, LoanDto loan) {
        if (applicant.getCreditScore() < MINIMUM_CREDIT_SCORE) {
            return Optional.of(RejectionReason.LOW_CREDIT_SCORE);
        }
        return Optional.empty();
    }
}
