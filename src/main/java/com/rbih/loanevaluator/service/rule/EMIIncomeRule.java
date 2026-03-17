package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.ApplicantDto;
import com.rbih.loanevaluator.dto.LoanDto;
import com.rbih.loanevaluator.enums.RejectionReason;
import com.rbih.loanevaluator.service.calculator.EMICalculator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
@Order(3)
public class EMIIncomeRule implements EligibilityRule {

    private static final BigDecimal BASE_RATE = new BigDecimal("12.0");
    private static final BigDecimal SIXTY_PERCENT = new BigDecimal("0.60");

    private final EMICalculator emiCalculator;

    public EMIIncomeRule(EMICalculator emiCalculator) {
        this.emiCalculator = emiCalculator;
    }

    @Override
    public Optional<RejectionReason> evaluate(ApplicantDto applicant, LoanDto loan) {
        BigDecimal principal = BigDecimal.valueOf(loan.getAmount());
        BigDecimal emi = emiCalculator.calculate(principal, BASE_RATE, loan.getTenureMonths());
        BigDecimal monthlyIncome = BigDecimal.valueOf(applicant.getMonthlyIncome());
        BigDecimal maxAllowedEmi = monthlyIncome.multiply(SIXTY_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        if (emi.compareTo(maxAllowedEmi) > 0) {
            return Optional.of(RejectionReason.EMI_EXCEEDS_60_PERCENT);
        }
        return Optional.empty();
    }
}
