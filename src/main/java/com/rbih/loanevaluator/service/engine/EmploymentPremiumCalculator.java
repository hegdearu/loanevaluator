package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmploymentPremiumCalculator implements PremiumCalculator {

    @Override
    public BigDecimal calculate(ApplicantDto applicant, LoanDto loan, RiskBand riskBand) {
        if (applicant.getEmploymentType() == EmploymentType.SELF_EMPLOYED) {
            return new BigDecimal("1.0");
        }
        return BigDecimal.ZERO;
    }
}
