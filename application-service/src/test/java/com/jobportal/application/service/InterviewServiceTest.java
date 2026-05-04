package com.jobportal.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobportal.application.dto.InterviewRequest;
import com.jobportal.application.model.Interview;
import com.jobportal.application.outbox.OutboxEvent;
import com.jobportal.application.outbox.OutboxEventRepository;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.application.repository.InterviewRepository;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock InterviewRepository interviewRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock OutboxEventRepository outboxEventRepository;
    @InjectMocks InterviewService interviewService;

    private InterviewRequest req;
    private Interview savedInterview;

    @BeforeEach
    void setUp() {
        // inject ObjectMapper with JavaTimeModule
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        org.springframework.test.util.ReflectionTestUtils.setField(interviewService, "objectMapper", mapper);

        req = new InterviewRequest();
        req.setApplicationId("app-1");
        req.setCandidateId("cand-1");
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setMeetingLink("https://zoom.us/j/123");

        savedInterview = new Interview();
        savedInterview.setId("iv-1");
        savedInterview.setApplicationId("app-1");
        savedInterview.setCandidateId("cand-1");
        savedInterview.setRecruiterId("rec-1");
        savedInterview.setScheduledAt(req.getScheduledAt());
        savedInterview.setStatus("SCHEDULED");
    }

    @Test
    void schedule_success() {
        when(applicationRepository.existsById("app-1")).thenReturn(true);
        when(interviewRepository.save(any(Interview.class))).thenReturn(savedInterview);
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(i -> i.getArgument(0));

        Interview result = interviewService.schedule(req, "rec-1");

        assertThat(result.getId()).isEqualTo("iv-1");
        verify(interviewRepository).save(any(Interview.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void schedule_applicationNotFound_throwsResourceNotFound() {
        when(applicationRepository.existsById("app-1")).thenReturn(false);
        assertThatThrownBy(() -> interviewService.schedule(req, "rec-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("app-1");
    }

    @Test
    void updateStatus_success() {
        when(interviewRepository.findById("iv-1")).thenReturn(Optional.of(savedInterview));
        when(interviewRepository.save(any())).thenReturn(savedInterview);

        Interview result = interviewService.updateStatus("iv-1", "COMPLETED", "rec-1");

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void updateStatus_notOwner_throwsForbidden() {
        when(interviewRepository.findById("iv-1")).thenReturn(Optional.of(savedInterview));
        assertThatThrownBy(() -> interviewService.updateStatus("iv-1", "COMPLETED", "other-recruiter"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateStatus_invalidStatus_throwsBadRequest() {
        when(interviewRepository.findById("iv-1")).thenReturn(Optional.of(savedInterview));
        assertThatThrownBy(() -> interviewService.updateStatus("iv-1", "INVALID", "rec-1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatus_notFound_throwsResourceNotFound() {
        when(interviewRepository.findById("bad-id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> interviewService.updateStatus("bad-id", "COMPLETED", "rec-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_canceled_success() {
        when(interviewRepository.findById("iv-1")).thenReturn(Optional.of(savedInterview));
        when(interviewRepository.save(any())).thenReturn(savedInterview);
        Interview result = interviewService.updateStatus("iv-1", "canceled", "rec-1");
        assertThat(result).isNotNull();
    }

    @Test
    void getByApplicationId_returnsList() {
        when(interviewRepository.findByApplicationId("app-1")).thenReturn(List.of(savedInterview));
        List<Interview> result = interviewService.getByApplicationId("app-1");
        assertThat(result).hasSize(1);
    }

    @Test
    void getMyInterviews_recruiter_returnsList() {
        when(interviewRepository.findByRecruiterIdOrderByScheduledAtAsc("rec-1")).thenReturn(List.of(savedInterview));
        List<Interview> result = interviewService.getMyInterviews("rec-1", "RECRUITER");
        assertThat(result).hasSize(1);
    }

    @Test
    void getMyInterviews_jobSeeker_returnsList() {
        when(interviewRepository.findByCandidateIdOrderByScheduledAtAsc("cand-1")).thenReturn(List.of(savedInterview));
        List<Interview> result = interviewService.getMyInterviews("cand-1", "JOB_SEEKER");
        assertThat(result).hasSize(1);
    }
}
