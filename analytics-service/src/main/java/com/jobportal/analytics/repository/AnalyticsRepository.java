package com.jobportal.analytics.repository;

import com.jobportal.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEvent, String> {
    long countByEventType(String eventType);
    List<AnalyticsEvent> findByJobId(String jobId);
}
