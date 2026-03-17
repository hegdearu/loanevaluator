package com.rbih.loanevaluator.dto.response;

import com.rbih.loanevaluator.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRejectedResponse implements LoanApplicationResponse {

    private UUID applicationId;
    private final ApplicationStatus status = ApplicationStatus.REJECTED;
    private List<String> rejectionReasons;
}
