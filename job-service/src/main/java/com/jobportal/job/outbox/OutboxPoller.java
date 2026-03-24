package com.jobportal.job.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final JmsTemplate jmsTemplate;

    public OutboxPoller(OutboxEventRepository outboxEventRepository,
                        @Qualifier("jobJmsTemplate") JmsTemplate jmsTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        log.info("[OUTBOX] Processing {} pending event(s)", events.size());
        for (OutboxEvent event : events) {
            try {
                String payload = event.getPayload();
                String eventType = event.getEventType();
                jmsTemplate.send(event.getDestination(), session -> {
                    jakarta.jms.TextMessage msg = session.createTextMessage(payload);
                    // _type header tells consumer which class to deserialize into
                    String typeClass = resolveTypeClass(eventType);
                    msg.setStringProperty("_type", typeClass);
                    return msg;
                });
                event.setProcessed(true);
                outboxEventRepository.save(event);
                log.info("[OUTBOX] Sent eventType={} to={}", eventType, event.getDestination());
            } catch (Exception e) {
                log.error("[OUTBOX] Failed eventType={} dest={} error={}",
                    event.getEventType(), event.getDestination(), e.getMessage(), e);
            }
        }
    }

    private String resolveTypeClass(String eventType) {
        return switch (eventType) {
            case "JOB_CREATED" -> "com.jobportal.common.events.JobCreatedEvent";
            case "JOB_CLOSED"  -> "com.jobportal.common.events.JobClosedEvent";
            default -> eventType;
        };
    }
}
