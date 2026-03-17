package com.rbih.loanevaluator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbih.loanevaluator.enums.ApplicationStatus;
import com.rbih.loanevaluator.enums.RiskBand;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationApprovedResponse implements LoanApplicationResponse {

    private UUID applicationId;
    private final ApplicationStatus status = ApplicationStatus.APPROVED;
    private RiskBand riskBand;
    private OfferDto offer;
}
