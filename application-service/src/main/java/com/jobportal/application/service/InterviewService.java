package com.jobportal.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.application.client.UserServiceClient;
import com.jobportal.application.dto.InterviewRequest;
import com.jobportal.application.model.Interview;
import com.jobportal.application.outbox.OutboxEvent;
import com.jobportal.application.outbox.OutboxEventRepository;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.application.repository.InterviewRepository;
import com.jobportal.application.service.GoogleCalendarService.CalendarResult;
import com.jobportal.common.events.InterviewScheduledEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class InterviewService {

    public static final String INTERVIEW_SCHEDULED_ROUTING_KEY = "interview.scheduled";

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final GoogleCalendarService googleCalendarService;
    private final UserServiceClient userServiceClient;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            OutboxEventRepository outboxEventRepository,
                            ObjectMapper objectMapper,
                            GoogleCalendarService googleCalendarService,
                            UserServiceClient userServiceClient) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.googleCalendarService = googleCalendarService;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public Interview schedule(InterviewRequest req, String recruiterId) {
        if (!applicationRepository.existsById(req.getApplicationId()))
            throw new ResourceNotFoundException("Application not found: " + req.getApplicationId());

        if (interviewRepository.existsByApplicationIdAndScheduledAt(req.getApplicationId(), req.getScheduledAt()))
            throw new BadRequestException("An interview for this application is already scheduled at this time");

        // Fetch candidate and recruiter emails for calendar invites
        String candidateEmail = getEmail(req.getCandidateId());
        String recruiterEmail = getEmail(recruiterId);

        Interview interview = new Interview();
        interview.setApplicationId(req.getApplicationId());
        interview.setCandidateId(req.getCandidateId());
        interview.setRecruiterId(recruiterId);
        interview.setScheduledAt(req.getScheduledAt());
        interview.setStatus("SCHEDULED");

        // Create Google Calendar event (1 hour duration by default)
        CalendarResult calendarResult = googleCalendarService.createEvent(
                "HireHub Interview",
                req.getScheduledAt(),
                req.getScheduledAt().plusHours(1),
                candidateEmail,
                recruiterEmail);

        // Use Google Meet link if generated, otherwise fall back to provided link
        String meetLink = calendarResult.meetLink() != null ? calendarResult.meetLink() : req.getMeetingLink();
        interview.setMeetingLink(meetLink);
        interview.setGoogleCalendarEventId(calendarResult.eventId());

        Interview saved = interviewRepository.save(interview);

        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setInterviewId(saved.getId());
        event.setApplicationId(saved.getApplicationId());
        event.setCandidateId(saved.getCandidateId());
        event.setRecruiterId(saved.getRecruiterId());
        event.setScheduledAt(saved.getScheduledAt());
        event.setMeetingLink(saved.getMeetingLink());

        outboxEventRepository.save(new OutboxEvent(
                "INTERVIEW_SCHEDULED", INTERVIEW_SCHEDULED_ROUTING_KEY, toJson(event)));

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

        // Delete calendar event when interview is canceled
        if ("CANCELED".equals(normalized))
            googleCalendarService.deleteEvent(interview.getGoogleCalendarEventId());

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

    private String getEmail(String userId) {
        try {
            Map<String, Object> user = userServiceClient.getUserById(userId);
            Object email = user.get("email");
            return email != null ? email.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize InterviewScheduledEvent", e);
        }
    }
}
