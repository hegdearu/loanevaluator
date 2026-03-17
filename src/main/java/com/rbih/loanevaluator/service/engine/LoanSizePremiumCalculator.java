package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanSizePremiumCalculator implements PremiumCalculator {

    private static final BigDecimal THRESHOLD = new BigDecimal("1000000");

    @Override
    public BigDecimal calculate(ApplicantDto applicant, LoanDto loan, RiskBand riskBand) {
        BigDecimal amount = BigDecimal.valueOf(loan.getAmount());
        if (amount.compareTo(THRESHOLD) > 0) {
            return new BigDecimal("0.5");
        }
        return BigDecimal.ZERO;
    }
}
