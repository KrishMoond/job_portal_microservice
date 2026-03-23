package com.jobportal.analytics.consumer;

import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.service.MetricsService;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.events.ResumeUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);

    private final MetricsService metricsService;

    public AnalyticsConsumer(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @JmsListener(destination = "job.created.queue")
    public void onJobCreated(JobCreatedEvent event) {
        log.info("Analytics: Job created - {}", event.getJobId());
        AnalyticsEvent ae = new AnalyticsEvent();
        ae.setEventType("JOB_CREATED");
        ae.setJobId(event.getJobId());
        ae.setUserId(event.getRecruiterId());
        ae.setMetadata("title=" + event.getTitle());
        metricsService.record(ae);
    }

    @JmsListener(destination = "job.applied.queue")
    public void onJobApplied(JobAppliedEvent event) {
        log.info("Analytics: Application - job:{} candidate:{}", event.getJobId(), event.getCandidateId());
        AnalyticsEvent ae = new AnalyticsEvent();
        ae.setEventType("JOB_APPLIED");
        ae.setJobId(event.getJobId());
        ae.setUserId(event.getCandidateId());
        ae.setMetadata("applicationId=" + event.getApplicationId());
        metricsService.record(ae);
    }

    @JmsListener(destination = "resume.uploaded.queue")
    public void onResumeUploaded(ResumeUploadedEvent event) {
        log.info("Analytics: Resume uploaded by user {}", event.getUserId());
        AnalyticsEvent ae = new AnalyticsEvent();
        ae.setEventType("RESUME_UPLOADED");
        ae.setUserId(event.getUserId());
        ae.setMetadata("resumeId=" + event.getResumeId());
        metricsService.record(ae);
    }
}
