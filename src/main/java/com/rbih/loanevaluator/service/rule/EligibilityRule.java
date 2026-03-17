package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.request.ApplicantDto;
import com.rbih.loanevaluator.dto.request.LoanDto;
import com.rbih.loanevaluator.enums.RejectionReason;

import java.util.Optional;

public interface EligibilityRule {
    Optional<RejectionReason> evaluate(ApplicantDto applicant, LoanDto loan);
}
