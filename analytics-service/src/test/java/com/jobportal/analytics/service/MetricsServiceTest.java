package com.jobportal.analytics.service;

import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock AnalyticsRepository analyticsRepository;
    @InjectMocks MetricsService metricsService;

    @Test
    void record_savesEvent() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType("JOB_CREATED");
        metricsService.record(event);
        verify(analyticsRepository).save(event);
    }

    @Test
    void getSummary_returnsCorrectCounts() {
        when(analyticsRepository.countByEventType("JOB_CREATED")).thenReturn(5L);
        when(analyticsRepository.countByEventType("JOB_APPLIED")).thenReturn(10L);
        when(analyticsRepository.countByEventType("RESUME_UPLOADED")).thenReturn(3L);

        Map<String, Long> summary = metricsService.getSummary();

        assertThat(summary.get("jobsPosted")).isEqualTo(5L);
        assertThat(summary.get("applications")).isEqualTo(10L);
        assertThat(summary.get("resumeUploads")).isEqualTo(3L);
    }
}
