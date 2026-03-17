package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RiskBand;

import java.math.BigDecimal;

public interface PremiumCalculator {
    BigDecimal calculate(ApplicantDto applicant, LoanDto loan, RiskBand riskBand);
}
