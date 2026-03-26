package com.jobportal.application.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.application.outbox.OutboxEvent;
import com.jobportal.application.outbox.OutboxEventRepository;
import com.jobportal.common.events.JobAppliedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplicationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventPublisher.class);
    public static final String JOB_APPLIED_ROUTING_KEY        = "job.applied";
    public static final String INTERVIEW_SCHEDULED_ROUTING_KEY = "interview.scheduled";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ApplicationEventPublisher(OutboxEventRepository outboxEventRepository,
                                     ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publishJobApplied(JobAppliedEvent event) {
        outboxEventRepository.save(new OutboxEvent("JOB_APPLIED", JOB_APPLIED_ROUTING_KEY, toJson(event)));
        log.info("[OUTBOX] Saved JobAppliedEvent to outbox | applicationId={}", event.getApplicationId());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
