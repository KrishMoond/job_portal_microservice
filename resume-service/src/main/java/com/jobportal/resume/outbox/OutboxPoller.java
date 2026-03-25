package com.jobportal.resume.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPoller(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, 100));
        if (events.isEmpty()) return;

        log.info("[OUTBOX] Processing {} pending event(s)", events.size());
        for (OutboxEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(event.getDestination(), event.getPayload());
                event.setProcessed(true);
                outboxEventRepository.save(event);
                log.info("[OUTBOX] Sent eventType={} to={}", event.getEventType(), event.getDestination());
            } catch (Exception e) {
                log.error("[OUTBOX] Failed eventType={} dest={} error={}. Will retry on next poll.",
                    event.getEventType(), event.getDestination(), e.getMessage(), e);
            }
        }
    }
}
