package com.jobportal.job.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.job.outbox.OutboxEvent;
import com.jobportal.job.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobEventPublisherTest {

    @Mock OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JobEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new JobEventPublisher(outboxEventRepository, objectMapper);
    }

    @Test
    void publishJobCreated_savesOutboxEvent() {
        JobCreatedEvent event = new JobCreatedEvent("job-1", "Dev", "TechCorp",
                "Remote", "100k", "desc", "rec-1", "Engineering");
        when(outboxEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        publisher.publishJobCreated(event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("JOB_CREATED");
        assertThat(captor.getValue().getDestination()).isEqualTo("job.created");
        assertThat(captor.getValue().getPayload()).contains("job-1");
    }

    @Test
    void publishJobClosed_savesOutboxEvent() {
        JobClosedEvent event = new JobClosedEvent("job-1", "Dev Role", "rec-1");
        when(outboxEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        publisher.publishJobClosed(event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("JOB_CLOSED");
        assertThat(captor.getValue().getDestination()).isEqualTo("job.closed");
    }
}
