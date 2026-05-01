package com.jobportal.application.controller;

import com.jobportal.application.dto.ApplyRequest;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.service.ApplicationService;
import com.jobportal.common.dto.ApiResponse;
import com.jobportal.common.exception.ForbiddenException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobApplication>> apply(
            @Valid @RequestBody ApplyRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "") String role) {
        if (!"JOB_SEEKER".equals(role)) throw new ForbiddenException("Only job seekers can apply for jobs");
        // If X-User-Id is present (via gateway), override candidateId from header
        if (!userId.isBlank()) req.setCandidateId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(applicationService.apply(req), "Application submitted"));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getByJob(
            @PathVariable String jobId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "RECRUITER") String role) {
        if (!"RECRUITER".equals(role) && !"ADMIN".equals(role))
            throw new ForbiddenException("Only recruiters and admins can view job applications");
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByJobId(jobId, userId, role), "Applications fetched"));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getByCandidate(
            @PathVariable String candidateId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByCandidateId(candidateId, userId, role), "Applications fetched"));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<JobApplication>> updateStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody StatusUpdateRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "RECRUITER") String role) {
        if (!"RECRUITER".equals(role) && !"ADMIN".equals(role))
            throw new ForbiddenException("Only recruiters and admins can update application status");
        return ResponseEntity.ok(ApiResponse.success(applicationService.updateStatus(applicationId, req, userId, role), "Status updated"));
    }

    @PostMapping("/{applicationId}/offer-response")
    public ResponseEntity<ApiResponse<JobApplication>> respondToOffer(
            @PathVariable String applicationId,
            @RequestParam("accepted") boolean accepted,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "") String role) {
        if (!"JOB_SEEKER".equals(role) && !"ADMIN".equals(role))
            throw new ForbiddenException("Only job seekers can respond to offers");
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.respondToOffer(applicationId, accepted, userId, role),
                accepted ? "Offer accepted" : "Offer rejected"));
    }
}
