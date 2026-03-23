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

    public static final String JOB_CREATED_QUEUE = "job.created.queue";
    public static final String JOB_CLOSED_QUEUE  = "job.closed.queue";

    private final JmsTemplate jmsTemplate;

    public JobEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishJobCreated(JobCreatedEvent event) {
        log.info("Publishing JobCreatedEvent for jobId: {}", event.getJobId());
        jmsTemplate.convertAndSend(JOB_CREATED_QUEUE, event);
    }

    public void publishJobClosed(JobClosedEvent event) {
        log.info("Publishing JobClosedEvent for jobId: {}", event.getJobId());
        jmsTemplate.convertAndSend(JOB_CLOSED_QUEUE, event);
    }
}
