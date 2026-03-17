package com.rbih.loanevaluator.dto;

import com.rbih.loanevaluator.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDto {

    @NotBlank(message = "Applicant name is required")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 21, message = "Age must be at least 21")
    @Max(value = 60, message = "Age must not exceed 60")
    private Integer age;

    @NotNull(message = "Monthly income is required")
    @Positive(message = "Monthly income must be greater than 0")
    private Double monthlyIncome;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score must not exceed 900")
    private Integer creditScore;
}