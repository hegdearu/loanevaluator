package com.rbih.loanevaluator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Applicant details are required")
    @Valid
    private ApplicantDto applicant;

    @NotNull(message = "Loan details are required")
    @Valid
    private LoanDto loan;
}
