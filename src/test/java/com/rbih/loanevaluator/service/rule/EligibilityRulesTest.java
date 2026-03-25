package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.service.calculator.EMICalculator;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RejectionReason;
import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityRulesTest {

    @Nested
    @DisplayName("CreditScoreRule")
    class CreditScoreRuleTest {

        private CreditScoreRule rule;

        @BeforeEach
        void setUp() {
            rule = new CreditScoreRule();
        }

        @Test
        @DisplayName("Should reject when credit score is below 600")
        void shouldRejectLowCreditScore() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 580);
            LoanDto loan = buildLoan(500000, 36);

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.LOW_CREDIT_SCORE, result.get());
        }

        @Test
        @DisplayName("Should pass when credit score is exactly 600")
        void shouldPassExactly600() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 600);
            LoanDto loan = buildLoan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should pass when credit score is above 600")
        void shouldPassAbove600() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 750);
            LoanDto loan = buildLoan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should reject when credit score is 599 (boundary)")
        void shouldRejectAt599() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 599);
            LoanDto loan = buildLoan(500000, 36);

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.LOW_CREDIT_SCORE, result.get());
        }
    }

    @Nested
    @DisplayName("AgeTenureRule")
    class AgeTenureRuleTest {

        private AgeTenureRule rule;

        @BeforeEach
        void setUp() {
            rule = new AgeTenureRule();
        }

        @Test
        @DisplayName("Should reject when age + tenure in years > 65")
        void shouldRejectWhenAgePlusTenureExceeds65() {
            ApplicantDto applicant = buildApplicant(55, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(500000, 132); // 11 years -> 55 + 11 = 66

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED, result.get());
        }

        @Test
        @DisplayName("Should pass when age + tenure in years = 65")
        void shouldPassWhenExactly65() {
            ApplicantDto applicant = buildApplicant(53, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(500000, 144); // 12 years -> 53 + 12 = 65

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should pass when age + tenure in years < 65")
        void shouldPassWhenBelow65() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(500000, 36); // 3 years -> 30 + 3 = 33

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should handle fractional tenure years correctly")
        void shouldHandleFractionalTenure() {
            ApplicantDto applicant = buildApplicant(60, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(500000, 66); // 5.5 years -> 60 + 5.5 = 65.5

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED, result.get());
        }
    }

    @Nested
    @DisplayName("EMIIncomeRule")
    class EMIIncomeRuleTest {

        private EMIIncomeRule rule;

        @BeforeEach
        void setUp() {
            rule = new EMIIncomeRule(new EMICalculator());
        }

        @Test
        @DisplayName("Should reject when EMI exceeds 60% of monthly income")
        void shouldRejectHighEMI() {
            ApplicantDto applicant = buildApplicant(30, 20000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(5000000, 36);

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.EMI_EXCEEDS_60_PERCENT, result.get());
        }

        @Test
        @DisplayName("Should pass when EMI is within 60% of monthly income")
        void shouldPassWhenEMIWithinLimit() {
            ApplicantDto applicant = buildApplicant(30, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = buildLoan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }
    }

    private static ApplicantDto buildApplicant(int age, double income, EmploymentType employment, int creditScore) {
        ApplicantDto applicant = new ApplicantDto();
        applicant.setName("Test Applicant");
        applicant.setAge(age);
        applicant.setMonthlyIncome(income);
        applicant.setEmploymentType(employment);
        applicant.setCreditScore(creditScore);
        return applicant;
    }

    private static LoanDto buildLoan(double amount, int tenureMonths) {
        LoanDto loan = new LoanDto();
        loan.setAmount(amount);
        loan.setTenureMonths(tenureMonths);
        loan.setPurpose(LoanPurpose.PERSONAL);
        return loan;
    }
}