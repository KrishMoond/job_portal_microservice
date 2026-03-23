package com.jobportal.application.controller;

import com.jobportal.application.dto.ApplyRequest;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.service.ApplicationService;
import com.jobportal.common.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<JobApplication>> apply(@Valid @RequestBody ApplyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(applicationService.apply(req), "Application submitted"));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getByJob(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByJobId(jobId), "Applications fetched"));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getByCandidate(@PathVariable String candidateId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByCandidateId(candidateId), "Applications fetched"));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<JobApplication>> updateStatus(
            @PathVariable String applicationId, @Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.updateStatus(applicationId, req), "Status updated"));
    }
}
