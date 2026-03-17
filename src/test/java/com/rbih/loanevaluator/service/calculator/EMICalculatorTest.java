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

        // Expected EMI ~ 16607.15 (verified with standard EMI calculators)
        assertTrue(emi.compareTo(new BigDecimal("16600")) > 0);
        assertTrue(emi.compareTo(new BigDecimal("16700")) < 0);
        assertEquals(2, emi.scale(), "EMI should have scale of 2");
    }

    @Test
    @DisplayName("Calculate EMI for small loan: 10000 at 12% for 6 months")
    void shouldCalculateEMIForSmallLoan() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("10000"),
                new BigDecimal("12.0"),
                6
        );

        // Short tenure, small amount — EMI should be roughly 1723
        assertTrue(emi.compareTo(new BigDecimal("1700")) > 0);
        assertTrue(emi.compareTo(new BigDecimal("1750")) < 0);
    }

    @Test
    @DisplayName("Calculate EMI for large loan: 5000000 at 15.5% for 360 months")
    void shouldCalculateEMIForLargeLoan() {
        BigDecimal emi = emiCalculator.calculate(
                new BigDecimal("5000000"),
                new BigDecimal("15.5"),
                360
        );

        // Long tenure, high rate — EMI should be positive and reasonable
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(emi.compareTo(new BigDecimal("5000000")) < 0,
                "Monthly EMI should be less than total principal");
    }

    @Test
    @DisplayName("Total payable should equal EMI multiplied by tenure")
    void shouldCalculateTotalPayable() {
        BigDecimal emi = new BigDecimal("16607.15");
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, 36);

        assertEquals(new BigDecimal("597857.40"), totalPayable);
    }

    @Test
    @DisplayName("EMI should increase with higher interest rate for same principal and tenure")
    void emiShouldIncreaseWithHigherRate() {
        BigDecimal principal = new BigDecimal("500000");
        int tenure = 36;

        BigDecimal emiLowRate = emiCalculator.calculate(principal, new BigDecimal("12.0"), tenure);
        BigDecimal emiHighRate = emiCalculator.calculate(principal, new BigDecimal("15.0"), tenure);

        assertTrue(emiHighRate.compareTo(emiLowRate) > 0,
                "Higher interest rate should produce higher EMI");
    }

    @Test
    @DisplayName("EMI should decrease with longer tenure for same principal and rate")
    void emiShouldDecreaseWithLongerTenure() {
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal rate = new BigDecimal("12.0");

        BigDecimal emiShort = emiCalculator.calculate(principal, rate, 12);
        BigDecimal emiLong = emiCalculator.calculate(principal, rate, 60);

        assertTrue(emiShort.compareTo(emiLong) > 0,
                "Shorter tenure should produce higher EMI");
    }
}
