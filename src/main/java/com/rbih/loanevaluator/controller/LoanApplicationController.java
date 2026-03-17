package com.rbih.loanevaluator.controller;

import com.rbih.loanevaluator.dto.LoanApplicationRequest;
import com.rbih.loanevaluator.dto.LoanApplicationResponse;
import com.rbih.loanevaluator.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        LoanApplicationResponse response = loanApplicationService.evaluate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
