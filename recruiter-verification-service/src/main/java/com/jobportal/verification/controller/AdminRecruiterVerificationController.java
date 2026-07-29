package com.jobportal.verification.controller;

import com.jobportal.verification.dto.RecruiterSubmissionDto;
import com.jobportal.verification.dto.ReviewRequest;
import com.jobportal.verification.model.VerificationStatus;
import com.jobportal.verification.service.RecruiterVerificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/recruiter-verifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecruiterVerificationController {
    private final RecruiterVerificationService service;

    public AdminRecruiterVerificationController(RecruiterVerificationService service) {
        this.service = service;
    }

    @GetMapping("/queue")
    public Page<RecruiterSubmissionDto> listQueue(
        @RequestParam(required = false) VerificationStatus status,
        @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.listQueue(status, pageable);
    }

    @GetMapping("/{id}")
    public RecruiterSubmissionDto getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @PostMapping("/{id}/review")
    public RecruiterSubmissionDto submitDecision(@PathVariable Long id, @Valid @RequestBody ReviewRequest request, Authentication authentication) {
        String reviewerId = request.reviewerId() != null ? request.reviewerId() : authentication.getName();
        return service.reviewRecruiter(id, request.decision(), request.reason(), reviewerId);
    }

    @GetMapping("/history")
    public Page<RecruiterSubmissionDto> listHistory(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.listHistory(search, pageable);
    }
}
