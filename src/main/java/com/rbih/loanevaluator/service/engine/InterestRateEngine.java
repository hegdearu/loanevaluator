package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InterestRateEngine {

    private static final BigDecimal BASE_RATE = new BigDecimal("12.0");

    private final List<PremiumCalculator> premiumCalculators;

    public InterestRateEngine(List<PremiumCalculator> premiumCalculators) {
        this.premiumCalculators = premiumCalculators;
    }

    public BigDecimal calculateRate(ApplicantDto applicant, LoanDto loan, RiskBand riskBand) {
        BigDecimal totalPremium = premiumCalculators.stream()
                .map(calc -> calc.calculate(applicant, loan, riskBand))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BASE_RATE.add(totalPremium).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getBaseRate() {
        return BASE_RATE;
    }
}
