package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RiskBand;
import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
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
    @DisplayName("Salaried + LOW risk + small loan = 12.00%")
    void shouldReturnBaseRateForBestCase() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SALARIED);
        LoanDto loan = buildLoan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("12.00"), rate);
    }

    @Test
    @DisplayName("Salaried + MEDIUM risk + small loan = 13.50%")
    void shouldAddRiskPremiumForMedium() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SALARIED);
        LoanDto loan = buildLoan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.MEDIUM);

        assertEquals(new BigDecimal("13.50"), rate);
    }

    @Test
    @DisplayName("Salaried + HIGH risk + small loan = 15.00%")
    void shouldAddRiskPremiumForHigh() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SALARIED);
        LoanDto loan = buildLoan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.HIGH);

        assertEquals(new BigDecimal("15.00"), rate);
    }

    @Test
    @DisplayName("Self-employed + HIGH risk + large loan = 16.50% (all premiums)")
    void shouldStackAllPremiums() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SELF_EMPLOYED);
        LoanDto loan = buildLoan(1500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.HIGH);

        assertEquals(new BigDecimal("16.50"), rate);
    }

    @Test
    @DisplayName("Self-employed + LOW risk + small loan = 13.00%")
    void shouldAddOnlyEmploymentPremium() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SELF_EMPLOYED);
        LoanDto loan = buildLoan(500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("13.00"), rate);
    }

    @Test
    @DisplayName("Salaried + LOW risk + large loan = 12.50%")
    void shouldAddOnlyLoanSizePremium() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SALARIED);
        LoanDto loan = buildLoan(2000000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.LOW);

        assertEquals(new BigDecimal("12.50"), rate);
    }

    @Test
    @DisplayName("Self-employed + MEDIUM risk + large loan = 15.00%")
    void shouldStackEmploymentAndLoanSizeWithMediumRisk() {
        ApplicantDto applicant = buildApplicant(EmploymentType.SELF_EMPLOYED);
        LoanDto loan = buildLoan(1500000);

        BigDecimal rate = engine.calculateRate(applicant, loan, RiskBand.MEDIUM);

        assertEquals(new BigDecimal("15.00"), rate);
    }

    private static ApplicantDto buildApplicant(EmploymentType employmentType) {
        ApplicantDto applicant = new ApplicantDto();
        applicant.setName("Test");
        applicant.setAge(30);
        applicant.setMonthlyIncome(75000.0);
        applicant.setEmploymentType(employmentType);
        applicant.setCreditScore(750);
        return applicant;
    }

    private static LoanDto buildLoan(double amount) {
        LoanDto loan = new LoanDto();
        loan.setAmount(amount);
        loan.setTenureMonths(36);
        loan.setPurpose(LoanPurpose.PERSONAL);
        return loan;
    }
}