package com.jobportal.notification.consumer;

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
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final EmailService emailService;
    private final NotificationLogRepository logRepository;
    private final InAppNotificationRepository inAppRepository;

    public NotificationConsumer(EmailService emailService,
                                NotificationLogRepository logRepository,
                                InAppNotificationRepository inAppRepository) {
        this.emailService = emailService;
        this.logRepository = logRepository;
        this.inAppRepository = inAppRepository;
    }

    // destination must exactly match JobEventPublisher.JOB_CREATED_QUEUE
    @JmsListener(destination = "job.created.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onJobCreated(JobCreatedEvent event) {
        log.info("[JMS-CONSUMER] Received JobCreatedEvent | jobId={} | title={}", event.getJobId(), event.getTitle());
        String subject = "New Job Posted: " + event.getTitle();
        String body = "A new job has been posted at " + event.getCompany()
            + " in " + event.getLocation() + ". Check it out!";
        if (event.getRecruiterId() != null)
            saveInAppNotification(event.getRecruiterId(), "Your job '" + event.getTitle() + "' has been posted successfully.");
        saveLog("JOB_CREATED", "subscribers@jobportal.com", subject, body);
        log.info("[JMS-CONSUMER] Processed JobCreatedEvent | jobId={}", event.getJobId());
    }

    @JmsListener(destination = "interview.scheduled.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onInterviewScheduled(InterviewScheduledEvent event) {
        log.info("[JMS-CONSUMER] Received InterviewScheduledEvent | interviewId={}", event.getInterviewId());
        String body = "Your interview has been scheduled for " + event.getScheduledAt()
            + (event.getMeetingLink() != null ? ". Meeting link: " + event.getMeetingLink() : "");
        saveInAppNotification(event.getCandidateId(), body);
        saveLog("INTERVIEW_SCHEDULED", event.getCandidateId(), "Interview Scheduled", body);
        log.info("[JMS-CONSUMER] Processed InterviewScheduledEvent | interviewId={}", event.getInterviewId());
    }

    @JmsListener(destination = "job.applied.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onJobApplied(JobAppliedEvent event) {
        log.info("[JMS-CONSUMER] Received JobAppliedEvent | jobId={} | candidateId={}", event.getJobId(), event.getCandidateId());
        String subject = "Application Submitted: " + event.getJobTitle();
        String body = "Your application for " + event.getJobTitle() + " has been submitted successfully.";
        if (event.getCandidateEmail() != null) {
            emailService.sendEmail(event.getCandidateEmail(), subject, body);
        }
        saveInAppNotification(event.getCandidateId(), body);
        saveLog("JOB_APPLIED", event.getCandidateEmail(), subject, body);
        log.info("[JMS-CONSUMER] Processed JobAppliedEvent | jobId={}", event.getJobId());
    }

    @JmsListener(destination = "job.closed.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onJobClosed(JobClosedEvent event) {
        log.info("[JMS-CONSUMER] Received JobClosedEvent | jobId={}", event.getJobId());
        String subject = "Job Closed: " + event.getTitle();
        String body = "The job posting for " + event.getTitle() + " has been closed.";
        saveLog("JOB_CLOSED", "applicants@jobportal.com", subject, body);
        log.info("[JMS-CONSUMER] Processed JobClosedEvent | jobId={}", event.getJobId());
    }

    @JmsListener(destination = "resume.uploaded.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onResumeUploaded(ResumeUploadedEvent event) {
        log.info("[JMS-CONSUMER] Received ResumeUploadedEvent | userId={} | resumeId={}", event.getUserId(), event.getResumeId());
        String subject = "Resume Upload Successful";
        String body = "Your resume has been uploaded successfully. Resume ID: " + event.getResumeId();
        saveInAppNotification(event.getUserId(), body);
        saveLog("RESUME_UPLOADED", event.getUserId(), subject, body);
        log.info("[JMS-CONSUMER] Processed ResumeUploadedEvent | resumeId={}", event.getResumeId());
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
