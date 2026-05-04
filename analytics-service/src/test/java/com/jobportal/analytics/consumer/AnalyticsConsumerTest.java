package com.jobportal.analytics.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.service.MetricsService;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.events.ResumeUploadedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsConsumerTest {

    @Mock MetricsService metricsService;
    private AnalyticsConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new AnalyticsConsumer(metricsService, objectMapper);
    }

    @Test
    void onJobCreated_recordsEvent() throws Exception {
        JobCreatedEvent event = new JobCreatedEvent("job-1", "Dev", "TechCorp", "Remote",
                "100k", "desc", "rec-1", "Engineering");
        consumer.onJobCreated(objectMapper.writeValueAsString(event));

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(metricsService).record(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("JOB_CREATED");
        assertThat(captor.getValue().getJobId()).isEqualTo("job-1");
        assertThat(captor.getValue().getUserId()).isEqualTo("rec-1");
    }

    @Test
    void onJobCreated_invalidJson_doesNotThrow() {
        consumer.onJobCreated("bad-json");
        verify(metricsService, never()).record(any());
    }

    @Test
    void onJobApplied_recordsEvent() throws Exception {
        JobAppliedEvent event = new JobAppliedEvent("app-1", "job-1", "Dev", "cand-1", "cand@example.com");
        consumer.onJobApplied(objectMapper.writeValueAsString(event));

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(metricsService).record(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("JOB_APPLIED");
        assertThat(captor.getValue().getJobId()).isEqualTo("job-1");
        assertThat(captor.getValue().getUserId()).isEqualTo("cand-1");
    }

    @Test
    void onJobApplied_invalidJson_doesNotThrow() {
        consumer.onJobApplied("bad-json");
        verify(metricsService, never()).record(any());
    }

    @Test
    void onResumeUploaded_recordsEvent() throws Exception {
        ResumeUploadedEvent event = new ResumeUploadedEvent("res-1", "user-1", "/api/resumes/download/res-1");
        consumer.onResumeUploaded(objectMapper.writeValueAsString(event));

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(metricsService).record(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("RESUME_UPLOADED");
        assertThat(captor.getValue().getUserId()).isEqualTo("user-1");
    }

    @Test
    void onResumeUploaded_invalidJson_doesNotThrow() {
        consumer.onResumeUploaded("bad-json");
        verify(metricsService, never()).record(any());
    }
}
