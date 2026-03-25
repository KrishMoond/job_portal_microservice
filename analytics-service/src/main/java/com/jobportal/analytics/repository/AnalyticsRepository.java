package com.jobportal.analytics.repository;

import com.jobportal.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEvent, String> {
    long countByEventType(String eventType);
    List<AnalyticsEvent> findByJobId(String jobId);
    List<AnalyticsEvent> findByUserId(String userId);
}
