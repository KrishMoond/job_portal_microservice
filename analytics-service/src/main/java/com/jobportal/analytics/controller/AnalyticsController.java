package com.jobportal.analytics.controller;

import com.jobportal.analytics.service.MetricsService;
import com.jobportal.common.dto.ApiResponse;
import com.jobportal.common.exception.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final MetricsService metricsService;

    public AnalyticsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSummary(
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String role) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Only admins can access analytics");
        return ResponseEntity.ok(ApiResponse.success(metricsService.getSummary(), "Analytics summary"));
    }
}
