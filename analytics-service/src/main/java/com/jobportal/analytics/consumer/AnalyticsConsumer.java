package com.jobportal.analytics.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.service.MetricsService;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.events.ResumeUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);

    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    public AnalyticsConsumer(MetricsService metricsService, ObjectMapper objectMapper) {
        this.metricsService = metricsService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "job.created.analytics.queue")
    public void onJobCreated(String payload) {
        try {
            JobCreatedEvent event = objectMapper.readValue(payload, JobCreatedEvent.class);
            log.info("Analytics: Job created - {}", event.getJobId());
            AnalyticsEvent ae = new AnalyticsEvent();
            ae.setEventType("JOB_CREATED");
            ae.setJobId(event.getJobId());
            ae.setUserId(event.getRecruiterId());
            ae.setMetadata("title=" + event.getTitle());
            metricsService.record(ae);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "job.applied.analytics.queue")
    public void onJobApplied(String payload) {
        try {
            JobAppliedEvent event = objectMapper.readValue(payload, JobAppliedEvent.class);
            log.info("Analytics: Application - job:{} candidate:{}", event.getJobId(), event.getCandidateId());
            AnalyticsEvent ae = new AnalyticsEvent();
            ae.setEventType("JOB_APPLIED");
            ae.setJobId(event.getJobId());
            ae.setUserId(event.getCandidateId());
            ae.setMetadata("applicationId=" + event.getApplicationId());
            metricsService.record(ae);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobAppliedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "resume.uploaded.analytics.queue")
    public void onResumeUploaded(String payload) {
        try {
            ResumeUploadedEvent event = objectMapper.readValue(payload, ResumeUploadedEvent.class);
            log.info("Analytics: Resume uploaded by user {}", event.getUserId());
            AnalyticsEvent ae = new AnalyticsEvent();
            ae.setEventType("RESUME_UPLOADED");
            ae.setUserId(event.getUserId());
            ae.setMetadata("resumeId=" + event.getResumeId());
            metricsService.record(ae);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process ResumeUploadedEvent: {}", e.getMessage(), e);
        }
    }
}
