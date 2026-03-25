package com.jobportal.resume.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.service.ResumeService;
import com.jobportal.common.exception.ForbiddenException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeResponse>> upload(
            @Valid @RequestBody ResumeRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        if (!"JOB_SEEKER".equals(role)) throw new ForbiddenException("Only job seekers can upload resumes");
        if (!userId.isBlank()) req.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(resumeService.upload(req), "Resume uploaded"));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getById(
            @PathVariable String resumeId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String requestUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        
        ResumeResponse resume = resumeService.getById(resumeId);
        if (!"ADMIN".equals(role) && !"RECRUITER".equals(role) && !resume.getUserId().equals(requestUserId)) {
            throw new ForbiddenException("You can only view your own resume");
        }
        
        return ResponseEntity.ok(ApiResponse.success(resume, "Resume fetched"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getByUser(
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String requesterId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        if (!"ADMIN".equals(role) && !userId.equals(requesterId))
            throw new ForbiddenException("You can only view your own resumes");
        return ResponseEntity.ok(ApiResponse.success(resumeService.getByUserId(userId), "Resumes fetched"));
    }
}
