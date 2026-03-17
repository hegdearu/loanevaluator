package com.rbih.loanevaluator.dto.response;

import com.rbih.loanevaluator.enums.ApplicationStatus;
import com.rbih.loanevaluator.enums.RiskBand;
import lombok.*;

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
