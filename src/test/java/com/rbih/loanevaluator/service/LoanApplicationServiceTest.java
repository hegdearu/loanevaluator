package com.rbih.loanevaluator.service;

import com.rbih.loanevaluator.enums.ApplicationStatus;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RiskBand;
import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.response.LoanApplicationApprovedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationRejectedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationResponse;
import com.rbih.loanevaluator.dto.request.LoanApplicationRequest;
import com.rbih.loanevaluator.dto.request.LoanDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LoanApplicationServiceTest {

    @Autowired
    private LoanApplicationService service;

    @Test
    @DisplayName("Should approve a standard eligible application with correct offer details")
    void shouldApproveEligibleApplication() {
        LoanApplicationRequest request = buildRequest("Aravind", 30, 75000.0,
                EmploymentType.SALARIED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationApprovedResponse.class, response);
        LoanApplicationApprovedResponse approved = (LoanApplicationApprovedResponse) response;

        assertEquals(ApplicationStatus.APPROVED, approved.getStatus());
        assertEquals(RiskBand.LOW, approved.getRiskBand());
        assertNotNull(approved.getApplicationId());
        assertNotNull(approved.getOffer());
        assertEquals(new BigDecimal("12.00"), approved.getOffer().getInterestRate());
        assertEquals(36, approved.getOffer().getTenureMonths());
        assertEquals(new BigDecimal("16607.15"), approved.getOffer().getEmi());
        assertEquals(new BigDecimal("597857.40"), approved.getOffer().getTotalPayable());
    }

    @Test
    @DisplayName("Should reject when credit score is below 600")
    void shouldRejectLowCreditScore() {
        LoanApplicationRequest request = buildRequest("Test", 30, 75000.0,
                EmploymentType.SALARIED, 500, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationRejectedResponse.class, response);
        LoanApplicationRejectedResponse rejected = (LoanApplicationRejectedResponse) response;

        assertEquals(ApplicationStatus.REJECTED, rejected.getStatus());
        assertNotNull(rejected.getApplicationId());
        assertTrue(rejected.getRejectionReasons().contains("LOW_CREDIT_SCORE"));
    }

    @Test
    @DisplayName("Should reject when age + tenure exceeds 65")
    void shouldRejectAgeTenureExceeded() {
        LoanApplicationRequest request = buildRequest("Test", 55, 75000.0,
                EmploymentType.SALARIED, 750, 500000.0, 180, LoanPurpose.HOME);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationRejectedResponse.class, response);
        LoanApplicationRejectedResponse rejected = (LoanApplicationRejectedResponse) response;

        assertTrue(rejected.getRejectionReasons().contains("AGE_TENURE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Should collect multiple rejection reasons without short-circuiting")
    void shouldCollectMultipleRejectionReasons() {
        LoanApplicationRequest request = buildRequest("Test", 58, 20000.0,
                EmploymentType.SALARIED, 500, 5000000.0, 120, LoanPurpose.HOME);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationRejectedResponse.class, response);
        LoanApplicationRejectedResponse rejected = (LoanApplicationRejectedResponse) response;

        assertTrue(rejected.getRejectionReasons().size() >= 2);
        assertTrue(rejected.getRejectionReasons().contains("LOW_CREDIT_SCORE"));
        assertTrue(rejected.getRejectionReasons().contains("AGE_TENURE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Should classify MEDIUM risk for score 720 with correct rate")
    void shouldClassifyMediumRisk() {
        LoanApplicationRequest request = buildRequest("Test", 30, 100000.0,
                EmploymentType.SALARIED, 720, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationApprovedResponse.class, response);
        LoanApplicationApprovedResponse approved = (LoanApplicationApprovedResponse) response;

        assertEquals(RiskBand.MEDIUM, approved.getRiskBand());
        assertEquals(new BigDecimal("13.50"), approved.getOffer().getInterestRate());
    }

    @Test
    @DisplayName("Self-employed should get 1% higher interest rate than salaried")
    void shouldApplyEmploymentPremium() {
        LoanApplicationRequest salariedRequest = buildRequest("Salaried", 30, 100000.0,
                EmploymentType.SALARIED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationRequest selfEmployedRequest = buildRequest("SelfEmployed", 30, 100000.0,
                EmploymentType.SELF_EMPLOYED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationApprovedResponse salariedResponse =
                (LoanApplicationApprovedResponse) service.evaluate(salariedRequest);
        LoanApplicationApprovedResponse selfEmployedResponse =
                (LoanApplicationApprovedResponse) service.evaluate(selfEmployedRequest);

        assertEquals(new BigDecimal("12.00"), salariedResponse.getOffer().getInterestRate());
        assertEquals(new BigDecimal("13.00"), selfEmployedResponse.getOffer().getInterestRate());
    }

    @Test
    @DisplayName("Loan above 10 lakh should get 0.5% size premium")
    void shouldApplyLoanSizePremium() {
        LoanApplicationRequest smallLoan = buildRequest("Small", 30, 200000.0,
                EmploymentType.SALARIED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationRequest largeLoan = buildRequest("Large", 30, 200000.0,
                EmploymentType.SALARIED, 760, 1500000.0, 36, LoanPurpose.HOME);

        LoanApplicationApprovedResponse smallResponse =
                (LoanApplicationApprovedResponse) service.evaluate(smallLoan);
        LoanApplicationApprovedResponse largeResponse =
                (LoanApplicationApprovedResponse) service.evaluate(largeLoan);

        assertEquals(new BigDecimal("12.00"), smallResponse.getOffer().getInterestRate());
        assertEquals(new BigDecimal("12.50"), largeResponse.getOffer().getInterestRate());
    }

    @Test
    @DisplayName("Should approve with HIGH risk band for credit score 620")
    void shouldClassifyHighRisk() {
        LoanApplicationRequest request = buildRequest("Test", 25, 120000.0,
                EmploymentType.SALARIED, 620, 500000.0, 60, LoanPurpose.AUTO);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationApprovedResponse.class, response);
        LoanApplicationApprovedResponse approved = (LoanApplicationApprovedResponse) response;

        assertEquals(RiskBand.HIGH, approved.getRiskBand());
        assertEquals(new BigDecimal("15.00"), approved.getOffer().getInterestRate());
    }

    private LoanApplicationRequest buildRequest(String name, int age, double income,
                                                EmploymentType employment, int creditScore,
                                                double amount, int tenure, LoanPurpose purpose) {
        ApplicantDto applicant = new ApplicantDto();
        applicant.setName(name);
        applicant.setAge(age);
        applicant.setMonthlyIncome(income);
        applicant.setEmploymentType(employment);
        applicant.setCreditScore(creditScore);

        LoanDto loan = new LoanDto();
        loan.setAmount(amount);
        loan.setTenureMonths(tenure);
        loan.setPurpose(purpose);

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setApplicant(applicant);
        request.setLoan(loan);

        return request;
    }
}