package com.jobportal.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.*;
import com.jobportal.notification.model.InAppNotification;
import com.jobportal.notification.model.NotificationLog;
import com.jobportal.notification.repository.InAppNotificationRepository;
import com.jobportal.notification.repository.NotificationLogRepository;
import com.jobportal.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock EmailService emailService;
    @Mock NotificationLogRepository logRepository;
    @Mock InAppNotificationRepository inAppRepository;

    private NotificationConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        consumer = new NotificationConsumer(emailService, logRepository, inAppRepository, objectMapper);
    }

    // ── onJobCreated ──────────────────────────────────────────────────────────

    @Test
    void onJobCreated_withRecruiterId_savesInAppAndLog() throws Exception {
        JobCreatedEvent event = new JobCreatedEvent("job-1", "Dev", "TechCorp", "Remote",
                "100k", "desc", "rec-1", "Engineering");
        consumer.onJobCreated(objectMapper.writeValueAsString(event));
        verify(inAppRepository).save(any(InAppNotification.class));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onJobCreated_nullRecruiterId_skipsInApp() throws Exception {
        JobCreatedEvent event = new JobCreatedEvent("job-1", "Dev", "TechCorp", "Remote",
                "100k", "desc", null, "Engineering");
        consumer.onJobCreated(objectMapper.writeValueAsString(event));
        verify(inAppRepository, never()).save(any());
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onJobCreated_invalidJson_doesNotThrow() {
        consumer.onJobCreated("not-json");
        verify(logRepository, never()).save(any());
    }

    // ── onJobApplied ──────────────────────────────────────────────────────────

    @Test
    void onJobApplied_withEmailAndRecruiterId_sendsEmailAndTwoInApp() throws Exception {
        JobAppliedEvent event = new JobAppliedEvent("app-1", "job-1", "Dev Role",
                "cand-1", "cand@example.com", "rec-1");
        consumer.onJobApplied(objectMapper.writeValueAsString(event));
        verify(emailService).sendEmail(eq("cand@example.com"), anyString(), anyString());
        verify(inAppRepository, times(2)).save(any(InAppNotification.class));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onJobApplied_noEmail_skipsEmail() throws Exception {
        JobAppliedEvent event = new JobAppliedEvent("app-1", "job-1", "Dev Role", "cand-1", null);
        consumer.onJobApplied(objectMapper.writeValueAsString(event));
        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(inAppRepository).save(any(InAppNotification.class));
    }

    @Test
    void onJobApplied_noRecruiterId_onlyOneCandidateInApp() throws Exception {
        JobAppliedEvent event = new JobAppliedEvent("app-1", "job-1", "Dev Role", "cand-1", "cand@example.com");
        consumer.onJobApplied(objectMapper.writeValueAsString(event));
        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(inAppRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("cand-1");
    }

    @Test
    void onJobApplied_invalidJson_doesNotThrow() {
        consumer.onJobApplied("bad-json");
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ── onStatusChanged ───────────────────────────────────────────────────────

    @Test
    void onStatusChanged_shortlisted_sendsEmail() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "SHORTLISTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(emailService).sendEmail(eq("cand@example.com"), contains("shortlisted"), anyString());
        verify(inAppRepository).save(any());
    }

    @Test
    void onStatusChanged_interviewScheduled_sendsEmail() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "INTERVIEW_SCHEDULED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(emailService).sendEmail(eq("cand@example.com"), contains("Interview"), anyString());
    }

    @Test
    void onStatusChanged_hired_sendsEmail() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "HIRED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(emailService).sendEmail(eq("cand@example.com"), contains("Congratulations"), anyString());
    }

    @Test
    void onStatusChanged_rejected_sendsEmail() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "REJECTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(emailService).sendEmail(eq("cand@example.com"), anyString(), anyString());
    }

    @Test
    void onStatusChanged_offerAccepted_notifiesRecruiter() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "OFFER_ACCEPTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(inAppRepository, times(2)).save(any());
    }

    @Test
    void onStatusChanged_offerRejected_notifiesRecruiter() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "OFFER_REJECTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(inAppRepository, times(2)).save(any());
    }

    @Test
    void onStatusChanged_defaultStatus_savesLog() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", "cand-1", "cand@example.com", "APPLIED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onStatusChanged_nullJobTitle_usesDefault() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", null, "cand-1", "cand@example.com", "SHORTLISTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onStatusChanged_nullCandidateId_skipsInApp() throws Exception {
        ApplicationStatusChangedEvent e = new ApplicationStatusChangedEvent(
                "app-1", "job-1", "Dev", null, "cand@example.com", "SHORTLISTED", "rec-1");
        consumer.onStatusChanged(objectMapper.writeValueAsString(e));
        verify(inAppRepository, never()).save(any());
    }

    @Test
    void onStatusChanged_invalidJson_doesNotThrow() {
        consumer.onStatusChanged("bad-json");
        verify(logRepository, never()).save(any());
    }

    // ── onInterviewScheduled ──────────────────────────────────────────────────

    @Test
    void onInterviewScheduled_withMeetingLink_savesInAppAndLog() throws Exception {
        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setInterviewId("iv-1");
        event.setCandidateId("cand-1");
        event.setRecruiterId("rec-1");
        event.setScheduledAt(LocalDateTime.now().plusDays(1));
        event.setMeetingLink("https://zoom.us/j/123");
        consumer.onInterviewScheduled(objectMapper.writeValueAsString(event));
        verify(inAppRepository, times(2)).save(any(InAppNotification.class));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onInterviewScheduled_noMeetingLink_savesInApp() throws Exception {
        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setInterviewId("iv-1");
        event.setCandidateId("cand-1");
        event.setRecruiterId("rec-1");
        event.setScheduledAt(LocalDateTime.now().plusDays(1));
        consumer.onInterviewScheduled(objectMapper.writeValueAsString(event));
        verify(inAppRepository, atLeast(1)).save(any(InAppNotification.class));
    }

    @Test
    void onInterviewScheduled_noRecruiterId_onlyCandidateInApp() throws Exception {
        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setInterviewId("iv-1");
        event.setCandidateId("cand-1");
        event.setScheduledAt(LocalDateTime.now().plusDays(1));
        consumer.onInterviewScheduled(objectMapper.writeValueAsString(event));
        verify(inAppRepository, times(1)).save(any(InAppNotification.class));
    }

    @Test
    void onInterviewScheduled_invalidJson_doesNotThrow() {
        consumer.onInterviewScheduled("bad-json");
        verify(inAppRepository, never()).save(any());
    }

    // ── onJobClosed ───────────────────────────────────────────────────────────

    @Test
    void onJobClosed_withRecruiterId_savesInAppAndLog() throws Exception {
        JobClosedEvent event = new JobClosedEvent("job-1", "Dev Role", "rec-1");
        consumer.onJobClosed(objectMapper.writeValueAsString(event));
        verify(inAppRepository).save(any(InAppNotification.class));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onJobClosed_noRecruiterId_skipsInApp() throws Exception {
        JobClosedEvent event = new JobClosedEvent();
        event.setJobId("job-1");
        event.setTitle("Dev Role");
        consumer.onJobClosed(objectMapper.writeValueAsString(event));
        verify(inAppRepository, never()).save(any());
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onJobClosed_invalidJson_doesNotThrow() {
        consumer.onJobClosed("bad-json");
        verify(logRepository, never()).save(any());
    }

    // ── onResumeUploaded ──────────────────────────────────────────────────────

    @Test
    void onResumeUploaded_savesInAppAndLog() throws Exception {
        ResumeUploadedEvent event = new ResumeUploadedEvent("res-1", "user-1", "/api/resumes/download/res-1");
        consumer.onResumeUploaded(objectMapper.writeValueAsString(event));
        verify(inAppRepository).save(any(InAppNotification.class));
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void onResumeUploaded_invalidJson_doesNotThrow() {
        consumer.onResumeUploaded("bad-json");
        verify(inAppRepository, never()).save(any());
    }
}
