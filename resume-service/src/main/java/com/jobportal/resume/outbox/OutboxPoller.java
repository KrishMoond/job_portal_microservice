package com.jobportal.resume.outbox;

import com.jobportal.resume.messaging.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("!test")
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationContext applicationContext;

    public OutboxPoller(OutboxEventRepository outboxEventRepository,
                        RabbitTemplate rabbitTemplate,
                        ApplicationContext applicationContext) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationContext = applicationContext;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository
            .findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, 100));
        if (events.isEmpty()) return;

        log.info("[OUTBOX] Processing {} pending event(s)", events.size());
        OutboxPoller proxy = applicationContext.getBean(OutboxPoller.class);
        for (OutboxEvent event : events) {
            proxy.processOne(event);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(OutboxEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, event.getDestination(), event.getPayload());
            event.setProcessed(true);
            outboxEventRepository.save(event);
            log.info("[OUTBOX] Sent eventType={} routingKey={}", event.getEventType(), event.getDestination());
        } catch (Exception e) {
            log.error("[OUTBOX] Failed eventType={} routingKey={} error={}. Will retry on next poll.",
                event.getEventType(), event.getDestination(), e.getMessage(), e);
            throw e;
        }
    }
}
