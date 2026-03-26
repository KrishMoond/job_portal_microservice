package com.jobportal.resume.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.resume.outbox.OutboxEvent;
import com.jobportal.resume.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResumeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResumeEventPublisher.class);
    public static final String RESUME_UPLOADED_ROUTING_KEY = "resume.uploaded";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ResumeEventPublisher(OutboxEventRepository outboxEventRepository,
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publishResumeUploaded(ResumeUploadedEvent event) {
        outboxEventRepository.save(new OutboxEvent("RESUME_UPLOADED", RESUME_UPLOADED_ROUTING_KEY, toJson(event)));
        log.info("[OUTBOX] Saved ResumeUploadedEvent to outbox | resumeId={}", event.getResumeId());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
