package com.jobportal.analytics.controller;

import com.jobportal.analytics.service.MetricsService;
import com.jobportal.common.exception.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final MetricsService metricsService;

    public AnalyticsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary(
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "") String role) {
        if (!"ADMIN".equals(role) && !"RECRUITER".equals(role))
            throw new ForbiddenException("Only recruiters and admins can access analytics");
        return ResponseEntity.ok(metricsService.getSummary());
    }
}
