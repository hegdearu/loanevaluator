package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RejectionReason;
import com.rbih.loanevaluator.service.calculator.EMICalculator;
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
            ApplicantDto applicant = applicant(30, 75000, EmploymentType.SALARIED, 580);
            LoanDto loan = loan(500000, 36);

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.LOW_CREDIT_SCORE, result.get());
        }

        @Test
        @DisplayName("Should pass when credit score is exactly 600")
        void shouldPassExactly600() {
            ApplicantDto applicant = applicant(30, 75000, EmploymentType.SALARIED, 600);
            LoanDto loan = loan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should pass when credit score is above 600")
        void shouldPassAbove600() {
            ApplicantDto applicant = applicant(30, 75000, EmploymentType.SALARIED, 750);
            LoanDto loan = loan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
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
            ApplicantDto applicant = applicant(55, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(500000, 132); // 11 years → 55 + 11 = 66

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED, result.get());
        }

        @Test
        @DisplayName("Should pass when age + tenure in years = 65")
        void shouldPassWhenExactly65() {
            ApplicantDto applicant = applicant(53, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(500000, 144); // 12 years → 53 + 12 = 65

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should pass when age + tenure in years < 65")
        void shouldPassWhenBelow65() {
            ApplicantDto applicant = applicant(30, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(500000, 36); // 3 years → 30 + 3 = 33

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }

        @Test
        @DisplayName("Should handle fractional tenure years correctly")
        void shouldHandleFractionalTenure() {
            ApplicantDto applicant = applicant(60, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(500000, 66); // 5.5 years → 60 + 5.5 = 65.5

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
            // Low income, high loan → EMI will exceed 60%
            ApplicantDto applicant = applicant(30, 20000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(5000000, 36);

            Optional<RejectionReason> result = rule.evaluate(applicant, loan);

            assertTrue(result.isPresent());
            assertEquals(RejectionReason.EMI_EXCEEDS_60_PERCENT, result.get());
        }

        @Test
        @DisplayName("Should pass when EMI is within 60% of monthly income")
        void shouldPassWhenEMIWithinLimit() {
            ApplicantDto applicant = applicant(30, 75000, EmploymentType.SALARIED, 720);
            LoanDto loan = loan(500000, 36);

            assertTrue(rule.evaluate(applicant, loan).isEmpty());
        }
    }

    // Test data helpers
    private static ApplicantDto applicant(int age, double income, EmploymentType employment, int creditScore) {
        return new ApplicantDto("Test Applicant", age, income, employment, creditScore);
    }

    private static LoanDto loan(double amount, int tenureMonths) {
        return new LoanDto(amount, tenureMonths, LoanPurpose.PERSONAL);
    }
}
