package com.jobportal.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.InterviewScheduledEvent;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.notification.model.NotificationLog;
import com.jobportal.notification.repository.NotificationLogRepository;
import com.jobportal.notification.model.InAppNotification;
import com.jobportal.notification.repository.InAppNotificationRepository;
import com.jobportal.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final EmailService emailService;
    private final NotificationLogRepository logRepository;
    private final InAppNotificationRepository inAppRepository;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(EmailService emailService,
                                NotificationLogRepository logRepository,
                                InAppNotificationRepository inAppRepository,
                                ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.logRepository = logRepository;
        this.inAppRepository = inAppRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "job.created.notification.queue")
    public void onJobCreated(String payload) {
        try {
            JobCreatedEvent event = objectMapper.readValue(payload, JobCreatedEvent.class);
            log.info("[AMQP-CONSUMER] Received JobCreatedEvent | jobId={}", event.getJobId());
            String subject = "New Job Posted: " + event.getTitle();
            String body = "A new job has been posted at " + event.getCompany()
                + " in " + event.getLocation() + ". Check it out!";
            if (event.getRecruiterId() != null)
                saveInAppNotification(event.getRecruiterId(), "Your job '" + event.getTitle() + "' has been posted successfully.");
            saveLog("JOB_CREATED", "subscribers@jobportal.com", subject, body);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "job.applied.notification.queue")
    public void onJobApplied(String payload) {
        try {
            JobAppliedEvent event = objectMapper.readValue(payload, JobAppliedEvent.class);
            log.info("[AMQP-CONSUMER] Received JobAppliedEvent | jobId={} | candidateId={}", event.getJobId(), event.getCandidateId());
            String subject = "Application Submitted: " + event.getJobTitle();
            String body = "Your application for " + event.getJobTitle() + " has been submitted successfully.";
            if (event.getCandidateEmail() != null) {
                emailService.sendEmail(event.getCandidateEmail(), subject, body);
            }
            saveInAppNotification(event.getCandidateId(), body);
            saveLog("JOB_APPLIED", event.getCandidateEmail(), subject, body);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobAppliedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "interview.scheduled.notification.queue")
    public void onInterviewScheduled(String payload) {
        try {
            InterviewScheduledEvent event = objectMapper.readValue(payload, InterviewScheduledEvent.class);
            log.info("[AMQP-CONSUMER] Received InterviewScheduledEvent | interviewId={}", event.getInterviewId());
            String body = "Your interview has been scheduled for " + event.getScheduledAt()
                + (event.getMeetingLink() != null ? ". Meeting link: " + event.getMeetingLink() : "");
            saveInAppNotification(event.getCandidateId(), body);
            saveLog("INTERVIEW_SCHEDULED", event.getCandidateId(), "Interview Scheduled", body);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process InterviewScheduledEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "job.closed.notification.queue")
    public void onJobClosed(String payload) {
        try {
            JobClosedEvent event = objectMapper.readValue(payload, JobClosedEvent.class);
            log.info("[AMQP-CONSUMER] Received JobClosedEvent | jobId={}", event.getJobId());
            String subject = "Job Closed: " + event.getTitle();
            String body = "The job posting for " + event.getTitle() + " has been closed.";
            saveLog("JOB_CLOSED", "applicants@jobportal.com", subject, body);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobClosedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "resume.uploaded.notification.queue")
    public void onResumeUploaded(String payload) {
        try {
            ResumeUploadedEvent event = objectMapper.readValue(payload, ResumeUploadedEvent.class);
            log.info("[AMQP-CONSUMER] Received ResumeUploadedEvent | resumeId={}", event.getResumeId());
            String subject = "Resume Upload Successful";
            String body = "Your resume has been uploaded successfully. Resume ID: " + event.getResumeId();
            saveInAppNotification(event.getUserId(), body);
            saveLog("RESUME_UPLOADED", event.getUserId(), subject, body);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process ResumeUploadedEvent: {}", e.getMessage(), e);
        }
    }

    private void saveLog(String eventType, String recipient, String subject, String body) {
        NotificationLog notif = new NotificationLog();
        notif.setEventType(eventType);
        notif.setRecipientEmail(recipient);
        notif.setSubject(subject);
        notif.setBody(body);
        logRepository.save(notif);
    }

    private void saveInAppNotification(String userId, String message) {
        InAppNotification notif = new InAppNotification();
        notif.setUserId(userId);
        notif.setMessage(message);
        inAppRepository.save(notif);
    }
}
