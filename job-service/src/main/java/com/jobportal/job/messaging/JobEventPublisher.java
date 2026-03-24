package com.jobportal.job.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.job.outbox.OutboxEvent;
import com.jobportal.job.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JobEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobEventPublisher.class);

    public static final String JOB_CREATED_SEARCH_QUEUE       = "job.created.search.queue";
    public static final String JOB_CREATED_NOTIFICATION_QUEUE = "job.created.notification.queue";
    public static final String JOB_CREATED_ANALYTICS_QUEUE    = "job.created.analytics.queue";
    public static final String JOB_CLOSED_SEARCH_QUEUE        = "job.closed.search.queue";
    public static final String JOB_CLOSED_NOTIFICATION_QUEUE  = "job.closed.notification.queue";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public JobEventPublisher(OutboxEventRepository outboxEventRepository,
                             @Qualifier("jobObjectMapper") ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publishJobCreated(JobCreatedEvent event) {
        String payload = toJson(event);
        outboxEventRepository.save(new OutboxEvent("JOB_CREATED", JOB_CREATED_SEARCH_QUEUE, payload));
        outboxEventRepository.save(new OutboxEvent("JOB_CREATED", JOB_CREATED_NOTIFICATION_QUEUE, payload));
        outboxEventRepository.save(new OutboxEvent("JOB_CREATED", JOB_CREATED_ANALYTICS_QUEUE, payload));
        log.info("[OUTBOX] Saved JobCreatedEvent to outbox | jobId={}", event.getJobId());
    }

    @Transactional
    public void publishJobClosed(JobClosedEvent event) {
        String payload = toJson(event);
        outboxEventRepository.save(new OutboxEvent("JOB_CLOSED", JOB_CLOSED_SEARCH_QUEUE, payload));
        outboxEventRepository.save(new OutboxEvent("JOB_CLOSED", JOB_CLOSED_NOTIFICATION_QUEUE, payload));
        log.info("[OUTBOX] Saved JobClosedEvent to outbox | jobId={}", event.getJobId());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
