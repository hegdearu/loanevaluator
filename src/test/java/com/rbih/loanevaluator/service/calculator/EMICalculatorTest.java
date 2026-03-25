package com.rbih.loanevaluator.service.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EMICalculatorTest {

    private EMICalculator emiCalculator;

    @BeforeEach
    void setUp() {
        emiCalculator = new EMICalculator();
    }

    @Test
    @DisplayName("Calculate EMI for standard loan: 500000 at 12% for 36 months")
    void shouldCalculateEMIForStandardLoan() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("500000"),
                new BigDecimal("12.0"),
                36
        );

        assertEquals(new BigDecimal("16607.15"), emi);
    }

    @Test
    @DisplayName("Calculate EMI for small loan: 10000 at 12% for 6 months")
    void shouldCalculateEMIForSmallLoan() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("10000"),
                new BigDecimal("12.0"),
                6
        );

        assertEquals(new BigDecimal("1725.48"), emi);
    }

    @Test
    @DisplayName("Calculate EMI for large loan: 5000000 at 15.5% for 360 months")
    void shouldCalculateEMIForLargeLoan() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("5000000"),
                new BigDecimal("15.5"),
                360
        );

        assertEquals(new BigDecimal("65225.85"), emi);
    }

    @Test
    @DisplayName("Calculate EMI at maximum premium rate: 500000 at 16.5% for 36 months")
    void shouldCalculateEMIAtMaxPremiumRate() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("500000"),
                new BigDecimal("16.5"),
                36
        );

        assertEquals(new BigDecimal("17702.19"), emi);
    }

    @Test
    @DisplayName("Total payable should equal EMI multiplied by tenure")
    void shouldCalculateTotalPayable() {
        BigDecimal emi = new BigDecimal("16607.15");
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, 36);

        assertEquals(new BigDecimal("597857.40"), totalPayable);
    }

    @Test
    @DisplayName("EMI should increase with higher interest rate")
    void emiShouldIncreaseWithHigherRate() {
        BigDecimal principal = new BigDecimal("500000");
        int tenure = 36;

        BigDecimal emiLowRate = emiCalculator.calculate(principal, new BigDecimal("12.0"), tenure);
        BigDecimal emiHighRate = emiCalculator.calculate(principal, new BigDecimal("15.0"), tenure);

        assertTrue(emiHighRate.compareTo(emiLowRate) > 0,
                "Higher interest rate should produce higher EMI");
    }

    @Test
    @DisplayName("EMI should decrease with longer tenure")
    void emiShouldDecreaseWithLongerTenure() {
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal rate = new BigDecimal("12.0");

        BigDecimal emiShort = emiCalculator.calculate(principal, rate, 12);
        BigDecimal emiLong = emiCalculator.calculate(principal, rate, 60);

        assertTrue(emiShort.compareTo(emiLong) > 0,
                "Shorter tenure should produce higher EMI");
    }

    @Test
    @DisplayName("EMI scale should always be 2")
    void emiScaleShouldAlwaysBeTwo() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("500000"),
                new BigDecimal("12.0"),
                36
        );

        assertEquals(2, emi.scale());
    }
}