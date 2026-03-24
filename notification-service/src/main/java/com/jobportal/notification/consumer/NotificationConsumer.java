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

    // destination must exactly match JobEventPublisher.JOB_CREATED_QUEUE
    @JmsListener(destination = "job.created.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onJobCreated(JobCreatedEvent event) {
        log.info("[JMS-CONSUMER] Received JobCreatedEvent | jobId={} | title={}", event.getJobId(), event.getTitle());
        String subject = "New Job Posted: " + event.getTitle();
        String body = "A new job has been posted at " + event.getCompany()
            + " in " + event.getLocation() + ". Check it out!";
        saveLog("JOB_CREATED", "subscribers@jobportal.com", subject, body);
        log.info("[JMS-CONSUMER] Processed JobCreatedEvent | jobId={}", event.getJobId());
    }

    @JmsListener(destination = "job.applied.notification.queue", containerFactory = "jmsListenerContainerFactory")
    public void onJobApplied(JobAppliedEvent event) {
        log.info("[JMS-CONSUMER] Received JobAppliedEvent | jobId={} | candidateId={}", event.getJobId(), event.getCandidateId());
        String subject = "Application Submitted: " + event.getJobTitle();
        String body = "Your application for " + event.getJobTitle() + " has been submitted successfully.";
        if (event.getCandidateEmail() != null) {
            emailService.sendEmail(event.getCandidateEmail(), subject, body);
        }
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
}
