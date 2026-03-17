package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterestRateEngineTest {

    private InterestRateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new InterestRateEngine(List.of(
                new RiskPremiumCalculator(),
                new EmploymentPremiumCalculator(),
                new LoanSizePremiumCalculator()
        ));
    }

    @Test
    @DisplayName("Salaried + LOW risk + small loan = base rate only (12.0%)")
    void shouldReturnBaseRateForBestCase() {
        ApplicantDto applicant = applicant(EmploymentType.SALARIED);
        LoanDto loan = loan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("12.00"), rate);
    }

    @Test
    @DisplayName("Salaried + MEDIUM risk + small loan = 12 + 1.5 = 13.5%")
    void shouldAddRiskPremiumForMedium() {
        ApplicantDto applicant = applicant(EmploymentType.SALARIED);
        LoanDto loan = loan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.MEDIUM);

        assertEquals(new BigDecimal("13.50"), rate);
    }

    @Test
    @DisplayName("Self-employed + HIGH risk + large loan = 12 + 3 + 1 + 0.5 = 16.5%")
    void shouldStackAllPremiums() {
        ApplicantDto applicant = applicant(EmploymentType.SELF_EMPLOYED);
        LoanDto loan = loan(1500000); // > 10 lakh

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.HIGH);

        assertEquals(new BigDecimal("16.50"), rate);
    }

    @Test
    @DisplayName("Self-employed + LOW risk + small loan = 12 + 0 + 1 + 0 = 13.0%")
    void shouldAddOnlyEmploymentPremium() {
        ApplicantDto applicant = applicant(EmploymentType.SELF_EMPLOYED);
        LoanDto loan = loan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("13.00"), rate);
    }

    @Test
    @DisplayName("Salaried + LOW risk + large loan = 12 + 0 + 0 + 0.5 = 12.5%")
    void shouldAddOnlyLoanSizePremium() {
        ApplicantDto applicant = applicant(EmploymentType.SALARIED);
        LoanDto loan = loan(2000000); // > 10 lakh

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("12.50"), rate);
    }

    private static ApplicantDto applicant(EmploymentType employmentType) {
        return new ApplicantDto("Test", 30, 75000.0, employmentType, 750);
    }

    private static LoanDto loan(double amount) {
        return new LoanDto(amount, 36, LoanPurpose.PERSONAL);
    }
}
