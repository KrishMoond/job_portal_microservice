package com.jobportal.job.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.dto.JobResponse;
import com.jobportal.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(
            @Valid @RequestBody JobRequest req,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(jobService.createJob(req, userId), "Job created"));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getById(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getById(jobId), "Job fetched"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(jobService.getAllJobs(), "Jobs fetched"));
    }

    @PutMapping("/{jobId}/close")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(
            @PathVariable String jobId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "RECRUITER") String role) {
        if (!"RECRUITER".equals(role) && !"ADMIN".equals(role))
            throw new ForbiddenException("Only recruiters and admins can close jobs");
        return ResponseEntity.ok(ApiResponse.success(jobService.closeJob(jobId, userId, role), "Job closed"));
    }
}
