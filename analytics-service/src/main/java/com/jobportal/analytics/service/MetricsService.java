package com.jobportal.analytics.service;

import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsService {

    private final AnalyticsRepository analyticsRepository;

    public MetricsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public void record(AnalyticsEvent event) {
        analyticsRepository.save(event);
    }

    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("jobsPosted",    analyticsRepository.countByEventType("JOB_CREATED"));
        summary.put("applications",  analyticsRepository.countByEventType("JOB_APPLIED"));
        summary.put("resumeUploads", analyticsRepository.countByEventType("RESUME_UPLOADED"));
        return summary;
    }
}
