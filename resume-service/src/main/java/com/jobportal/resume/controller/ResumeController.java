package com.jobportal.resume.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.service.ResumeService;
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
    public ResponseEntity<ApiResponse<ResumeResponse>> upload(@Valid @RequestBody ResumeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(resumeService.upload(req), "Resume uploaded"));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getById(@PathVariable String resumeId) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getById(resumeId), "Resume fetched"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getByUserId(userId), "Resumes fetched"));
    }
}
