package com.jobportal.application.controller;

import com.jobportal.application.dto.InterviewRequest;
import com.jobportal.application.model.Interview;
import com.jobportal.application.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<Interview> scheduleInterview(
            @Valid @RequestBody InterviewRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.schedule(req, userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Interview> updateStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.ok(interviewService.updateStatus(id, status, userId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Interview>> getByApplication(@PathVariable String applicationId) {
        return ResponseEntity.ok(interviewService.getByApplicationId(applicationId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Interview>> getMyInterviews(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        return ResponseEntity.ok(interviewService.getMyInterviews(userId, role));
    }
}
