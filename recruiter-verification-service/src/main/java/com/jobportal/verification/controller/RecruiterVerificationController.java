package com.jobportal.verification.controller;

import com.jobportal.verification.dto.CreateRecruiterSubmissionRequest;
import com.jobportal.verification.dto.RecruiterSubmissionDto;
import com.jobportal.verification.service.RecruiterVerificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter-verifications")
public class RecruiterVerificationController {
    private final RecruiterVerificationService service;

    public RecruiterVerificationController(RecruiterVerificationService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public RecruiterSubmissionDto createSubmission(@Valid @RequestBody CreateRecruiterSubmissionRequest request) {
        return service.createSubmission(request);
    }
}
