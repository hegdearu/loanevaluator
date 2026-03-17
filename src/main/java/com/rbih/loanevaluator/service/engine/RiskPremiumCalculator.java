package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RiskPremiumCalculator implements PremiumCalculator {

    @Override
    public BigDecimal calculate(ApplicantDto applicant, LoanDto loan, RiskBand riskBand) {
        return switch (riskBand) {
            case LOW -> BigDecimal.ZERO;
            case MEDIUM -> new BigDecimal("1.5");
            case HIGH -> new BigDecimal("3.0");
        };
    }
}
