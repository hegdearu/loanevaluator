package com.rbih.loanevaluator.service.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class EMICalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);
    private static final int FINAL_SCALE = 2;
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal monthlyRate = annualRatePercent
                .divide(MONTHS_IN_YEAR, MATH_CONTEXT)
                .divide(HUNDRED, MATH_CONTEXT);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);

        BigDecimal onePlusRPowerN = onePlusR.pow(tenureMonths, MATH_CONTEXT);

        BigDecimal numerator = principal
                .multiply(monthlyRate, MATH_CONTEXT)
                .multiply(onePlusRPowerN, MATH_CONTEXT);

        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE, MATH_CONTEXT);

        return numerator.divide(denominator, FINAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(new BigDecimal(tenureMonths))
                .setScale(FINAL_SCALE, RoundingMode.HALF_UP);
    }
}
