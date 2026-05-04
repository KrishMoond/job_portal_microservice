package com.jobportal.resume.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.resume.outbox.OutboxEvent;
import com.jobportal.resume.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeEventPublisherTest {

    @Mock OutboxEventRepository outboxEventRepository;
    @Mock ObjectMapper objectMapper;
    @InjectMocks ResumeEventPublisher publisher;

    @Test
    void publishResumeUploaded_savesOutboxEvent() throws Exception {
        ResumeUploadedEvent event = new ResumeUploadedEvent("resume-1", "user-1", "https://s3/resume.pdf");
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"resumeId\":\"resume-1\"}");

        publisher.publishResumeUploaded(event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("RESUME_UPLOADED");
        assertThat(captor.getValue().getDestination()).isEqualTo(ResumeEventPublisher.RESUME_UPLOADED_ROUTING_KEY);
    }

    @Test
    void publishResumeUploaded_serializationFailure_throwsRuntimeException() throws Exception {
        ResumeUploadedEvent event = new ResumeUploadedEvent("resume-1", "user-1", "https://s3/resume.pdf");
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("fail") {});

        assertThatThrownBy(() -> publisher.publishResumeUploaded(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("serialize");
    }
}
