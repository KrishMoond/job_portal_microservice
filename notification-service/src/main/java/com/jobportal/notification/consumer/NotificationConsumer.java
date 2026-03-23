package com.jobportal.notification.consumer;

import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.notification.model.NotificationLog;
import com.jobportal.notification.repository.NotificationLogRepository;
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

    public NotificationConsumer(EmailService emailService,
                                 NotificationLogRepository logRepository) {
        this.emailService = emailService;
        this.logRepository = logRepository;
    }

    @JmsListener(destination = "job.created.queue")
    public void onJobCreated(JobCreatedEvent event) {
        log.info("Notification: Job created - {}", event.getJobId());
        String subject = "New Job Posted: " + event.getTitle();
        String body = "A new job has been posted at " + event.getCompany()
            + " in " + event.getLocation() + ". Check it out!";
        saveLog("JOB_CREATED", "subscribers@jobportal.com", subject, body);
    }

    @JmsListener(destination = "job.applied.queue")
    public void onJobApplied(JobAppliedEvent event) {
        log.info("Notification: Candidate {} applied for job {}", event.getCandidateId(), event.getJobId());
        String subject = "Application Submitted: " + event.getJobTitle();
        String body = "Your application for " + event.getJobTitle() + " has been submitted successfully.";
        if (event.getCandidateEmail() != null) {
            emailService.sendEmail(event.getCandidateEmail(), subject, body);
        }
        saveLog("JOB_APPLIED", event.getCandidateEmail(), subject, body);
    }

    @JmsListener(destination = "job.closed.queue")
    public void onJobClosed(JobClosedEvent event) {
        log.info("Notification: Job closed - {}", event.getJobId());
        String subject = "Job Closed: " + event.getTitle();
        String body = "The job posting for " + event.getTitle() + " has been closed.";
        saveLog("JOB_CLOSED", "applicants@jobportal.com", subject, body);
    }

    @JmsListener(destination = "resume.uploaded.queue")
    public void onResumeUploaded(ResumeUploadedEvent event) {
        log.info("Notification: Resume uploaded by user {}", event.getUserId());
        String subject = "Resume Upload Successful";
        String body = "Your resume has been uploaded successfully. Resume ID: " + event.getResumeId();
        saveLog("RESUME_UPLOADED", event.getUserId(), subject, body);
    }

    private void saveLog(String eventType, String recipient, String subject, String body) {
        NotificationLog notif = new NotificationLog();
        notif.setEventType(eventType);
        notif.setRecipientEmail(recipient);
        notif.setSubject(subject);
        notif.setBody(body);
        logRepository.save(notif);
    }
}
