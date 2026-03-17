package com.rbih.loanevaluator.dto;

import com.rbih.loanevaluator.enums.LoanPurpose;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {

    @NotNull(message = "Loan amount is required")
    @Min(value = 10000, message = "Loan amount must be at least 10,000")
    @Max(value = 5000000, message = "Loan amount must not exceed 50,00,000")
    private Double amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 360, message = "Tenure must not exceed 360 months")
    private Integer tenureMonths;

    @NotNull(message = "Loan purpose is required")
    private LoanPurpose purpose;
}
