package com.rbih.loanevaluator.service;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanApplicationRequest;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.dto.response.LoanApplicationApprovedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationRejectedResponse;
import com.rbih.loanevaluator.dto.response.LoanApplicationResponse;
import com.rbih.loanevaluator.enums.ApplicationStatus;
import com.rbih.loanevaluator.enums.EmploymentType;
import com.rbih.loanevaluator.enums.LoanPurpose;
import com.rbih.loanevaluator.enums.RiskBand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoanApplicationServiceTest {

    @Autowired
    private LoanApplicationService service;

    @Test
    @DisplayName("Should approve a standard eligible application")
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
        assertEquals(36, approved.getOffer().getTenureMonths());
        assertTrue(approved.getOffer().getEmi().doubleValue() > 0);
        assertTrue(approved.getOffer().getTotalPayable().doubleValue() > approved.getOffer().getEmi().doubleValue());
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
    @DisplayName("Should collect multiple rejection reasons")
    void shouldCollectMultipleRejectionReasons() {
        LoanApplicationRequest request = buildRequest("Test", 58, 20000.0,
                EmploymentType.SALARIED, 500, 5000000.0, 120, LoanPurpose.HOME);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationRejectedResponse.class, response);
        LoanApplicationRejectedResponse rejected = (LoanApplicationRejectedResponse) response;

        assertTrue(rejected.getRejectionReasons().size() >= 2);
    }

    @Test
    @DisplayName("Should apply correct risk band MEDIUM for score 720")
    void shouldClassifyMediumRisk() {
        LoanApplicationRequest request = buildRequest("Test", 30, 100000.0,
                EmploymentType.SALARIED, 720, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationResponse response = service.evaluate(request);

        assertInstanceOf(LoanApplicationApprovedResponse.class, response);
        LoanApplicationApprovedResponse approved = (LoanApplicationApprovedResponse) response;

        assertEquals(RiskBand.MEDIUM, approved.getRiskBand());
    }

    @Test
    @DisplayName("Self-employed applicant should get higher interest rate")
    void shouldApplyEmploymentPremium() {
        LoanApplicationRequest salariedRequest = buildRequest("Salaried", 30, 100000.0,
                EmploymentType.SALARIED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationRequest selfEmployedRequest = buildRequest("SelfEmployed", 30, 100000.0,
                EmploymentType.SELF_EMPLOYED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationApprovedResponse salariedResponse =
                (LoanApplicationApprovedResponse) service.evaluate(salariedRequest);
        LoanApplicationApprovedResponse selfEmployedResponse =
                (LoanApplicationApprovedResponse) service.evaluate(selfEmployedRequest);

        assertTrue(selfEmployedResponse.getOffer().getInterestRate()
                        .compareTo(salariedResponse.getOffer().getInterestRate()) > 0,
                "Self-employed should have higher interest rate");
    }

    @Test
    @DisplayName("Should apply loan size premium for amount above 10 lakh")
    void shouldApplyLoanSizePremium() {
        LoanApplicationRequest smallLoan = buildRequest("Small", 30, 200000.0,
                EmploymentType.SALARIED, 760, 500000.0, 36, LoanPurpose.PERSONAL);

        LoanApplicationRequest largeLoan = buildRequest("Large", 30, 200000.0,
                EmploymentType.SALARIED, 760, 1500000.0, 36, LoanPurpose.HOME);

        LoanApplicationApprovedResponse smallResponse =
                (LoanApplicationApprovedResponse) service.evaluate(smallLoan);
        LoanApplicationApprovedResponse largeResponse =
                (LoanApplicationApprovedResponse) service.evaluate(largeLoan);

        assertTrue(largeResponse.getOffer().getInterestRate()
                        .compareTo(smallResponse.getOffer().getInterestRate()) > 0,
                "Large loan should have higher interest rate due to size premium");
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