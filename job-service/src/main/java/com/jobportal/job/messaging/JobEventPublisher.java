package com.jobportal.job.messaging;

import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobEventPublisher.class);

    // Queue names — must exactly match @JmsListener destination in all consumers
    public static final String JOB_CREATED_QUEUE = "job.created.queue";
    public static final String JOB_CLOSED_QUEUE  = "job.closed.queue";

    private final JmsTemplate jmsTemplate;

    public JobEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishJobCreated(JobCreatedEvent event) {
        try {
            jmsTemplate.convertAndSend(JOB_CREATED_QUEUE, event);
            log.info("[JMS-PRODUCER] Sent JobCreatedEvent to '{}' | jobId={}", JOB_CREATED_QUEUE, event.getJobId());
        } catch (Exception e) {
            log.error("[JMS-PRODUCER] Failed to send JobCreatedEvent | jobId={} | error={}", event.getJobId(), e.getMessage(), e);
        }
    }

    public void publishJobClosed(JobClosedEvent event) {
        try {
            jmsTemplate.convertAndSend(JOB_CLOSED_QUEUE, event);
            log.info("[JMS-PRODUCER] Sent JobClosedEvent to '{}' | jobId={}", JOB_CLOSED_QUEUE, event.getJobId());
        } catch (Exception e) {
            log.error("[JMS-PRODUCER] Failed to send JobClosedEvent | jobId={} | error={}", event.getJobId(), e.getMessage(), e);
        }
    }
}
