package com.jobportal.resume.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.model.Resume;
import com.jobportal.resume.repository.ResumeRepository;
import com.jobportal.resume.service.ResumeService;
import com.jobportal.common.exception.ForbiddenException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;

    public ResumeController(ResumeService resumeService, ResumeRepository resumeRepository) {
        this.resumeService = resumeService;
        this.resumeRepository = resumeRepository;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        if (!"JOB_SEEKER".equals(role)) throw new ForbiddenException("Only job seekers can upload resumes");
        if (userId.isBlank()) throw new ForbiddenException("Missing authenticated user");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resumeService.uploadFile(userId, file), "Resume uploaded"));
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

    @GetMapping("/download/{resumeId}")
    public ResponseEntity<?> download(
            @PathVariable String resumeId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String requestUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        ResumeResponse resume = resumeService.getById(resumeId);
        if (!"ADMIN".equals(role) && !"RECRUITER".equals(role) && !resume.getUserId().equals(requestUserId)) {
            throw new ForbiddenException("You can only view your own resume");
        }
        if ("DB".equalsIgnoreCase(resume.getStorageType())) {
            Resume entity = resumeRepository.findById(resumeId).orElseThrow();
            String contentType = entity.getContentType() != null ? entity.getContentType() : "application/octet-stream";
            String fileName = entity.getFileName() != null ? entity.getFileName() : "resume";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(entity.getFileData());
        }
        // URL-backed resumes still redirect as before.
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(resume.getFileUrl())).build();
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
