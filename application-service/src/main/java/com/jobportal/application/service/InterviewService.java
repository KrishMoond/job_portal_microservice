package com.jobportal.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.application.dto.InterviewRequest;
import com.jobportal.application.model.Interview;
import com.jobportal.application.outbox.OutboxEvent;
import com.jobportal.application.outbox.OutboxEventRepository;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.application.repository.InterviewRepository;
import com.jobportal.common.events.InterviewScheduledEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class InterviewService {

    public static final String INTERVIEW_SCHEDULED_NOTIFICATION_QUEUE = "interview.scheduled.notification.queue";

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            OutboxEventRepository outboxEventRepository,
                            ObjectMapper objectMapper) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Interview schedule(InterviewRequest req, String recruiterId) {
        if (!applicationRepository.existsById(req.getApplicationId()))
            throw new ResourceNotFoundException("Application not found: " + req.getApplicationId());

        Interview interview = new Interview();
        interview.setApplicationId(req.getApplicationId());
        interview.setCandidateId(req.getCandidateId());
        interview.setRecruiterId(recruiterId);
        interview.setScheduledAt(req.getScheduledAt());
        interview.setMeetingLink(req.getMeetingLink());
        interview.setStatus("SCHEDULED");
        Interview saved = interviewRepository.save(interview);

        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setInterviewId(saved.getId());
        event.setApplicationId(saved.getApplicationId());
        event.setCandidateId(saved.getCandidateId());
        event.setRecruiterId(saved.getRecruiterId());
        event.setScheduledAt(saved.getScheduledAt());
        event.setMeetingLink(saved.getMeetingLink());

        outboxEventRepository.save(new OutboxEvent(
            "INTERVIEW_SCHEDULED", INTERVIEW_SCHEDULED_NOTIFICATION_QUEUE, toJson(event)));

        return saved;
    }

    @Transactional
    public Interview updateStatus(String id, String status, String requesterId) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + id));
        if (!interview.getRecruiterId().equals(requesterId))
            throw new ForbiddenException("Only the assigned recruiter can update this interview");
        String normalized = status.toUpperCase(Locale.ROOT);
        if (!normalized.equals("SCHEDULED") && !normalized.equals("COMPLETED") && !normalized.equals("CANCELED"))
            throw new BadRequestException("Invalid status. Must be SCHEDULED, COMPLETED, or CANCELED");
        interview.setStatus(normalized);
        return interviewRepository.save(interview);
    }

    public List<Interview> getByApplicationId(String applicationId) {
        return interviewRepository.findByApplicationId(applicationId);
    }

    public List<Interview> getMyInterviews(String userId, String role) {
        if ("RECRUITER".equals(role))
            return interviewRepository.findByRecruiterIdOrderByScheduledAtAsc(userId);
        return interviewRepository.findByCandidateIdOrderByScheduledAtAsc(userId);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize InterviewScheduledEvent", e);
        }
    }
}
