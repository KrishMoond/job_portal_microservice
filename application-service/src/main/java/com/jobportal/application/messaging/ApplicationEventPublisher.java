package com.jobportal.application.messaging;

import com.jobportal.common.events.JobAppliedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventPublisher.class);
    public static final String JOB_APPLIED_QUEUE = "job.applied.queue";

    private final JmsTemplate appJmsTemplate;

    public ApplicationEventPublisher(JmsTemplate appJmsTemplate) {
        this.appJmsTemplate = appJmsTemplate;
    }

    public void publishJobApplied(JobAppliedEvent event) {
        log.info("Publishing JobAppliedEvent for applicationId: {}", event.getApplicationId());
        appJmsTemplate.convertAndSend(JOB_APPLIED_QUEUE, event);
    }
}
