package com.rbih.loanevaluator.service.rule;

import com.rbih.loanevaluator.dto.ApplicantDto;
import com.rbih.loanevaluator.dto.LoanDto;
import com.rbih.loanevaluator.enums.RejectionReason;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EligibilityEvaluator {

    private final List<EligibilityRule> rules;

    public EligibilityEvaluator(List<EligibilityRule> rules) {
        this.rules = rules;
    }

    public List<RejectionReason> evaluate(ApplicantDto applicant, LoanDto loan) {
        return rules.stream()
                .map(rule -> rule.evaluate(applicant, loan))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
