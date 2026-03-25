package com.jobportal.analytics.controller;

import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.repository.AnalyticsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final AnalyticsRepository analyticsRepository;

    public RecommendationController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @GetMapping
    public ResponseEntity<?> getRecommendations(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        // Returns jobs the user has interacted with via analytics events (JOB_APPLIED)
        List<Map<String, String>> recommendations = analyticsRepository
            .findByUserId(userId).stream()
            .filter(e -> "JOB_APPLIED".equals(e.getEventType()) && e.getJobId() != null)
            .map(e -> Map.of("jobId", e.getJobId(), "eventType", e.getEventType()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(recommendations);
    }
}
