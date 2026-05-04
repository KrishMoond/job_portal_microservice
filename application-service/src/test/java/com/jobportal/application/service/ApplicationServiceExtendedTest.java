package com.jobportal.application.service;

import com.jobportal.application.client.JobServiceClient;
import com.jobportal.application.client.UserServiceClient;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.messaging.ApplicationEventPublisher;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceExtendedTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock JobServiceClient jobServiceClient;
    @Mock UserServiceClient userServiceClient;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks ApplicationService applicationService;

    private JobApplication app;

    @BeforeEach
    void setUp() {
        app = new JobApplication();
        app.setId("app-1");
        app.setJobId("job-1");
        app.setCandidateId("cand-1");
        app.setRecruiterId("rec-1");
        app.setJobTitle("Dev");
        app.setStatus(JobApplication.Status.APPLIED);
    }

    // ── respondToOffer ────────────────────────────────────────────────────────

    @Test
    void respondToOffer_accept_success() {
        app.setStatus(JobApplication.Status.HIRED);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        doNothing().when(eventPublisher).publishStatusChanged(any());

        JobApplication result = applicationService.respondToOffer("app-1", true, "cand-1", "JOB_SEEKER");

        assertThat(result.getStatus()).isEqualTo(JobApplication.Status.OFFER_ACCEPTED);
    }

    @Test
    void respondToOffer_reject_success() {
        app.setStatus(JobApplication.Status.HIRED);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        doNothing().when(eventPublisher).publishStatusChanged(any());

        JobApplication result = applicationService.respondToOffer("app-1", false, "cand-1", "JOB_SEEKER");

        assertThat(result.getStatus()).isEqualTo(JobApplication.Status.OFFER_REJECTED);
    }

    @Test
    void respondToOffer_notHired_throwsBadRequest() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> applicationService.respondToOffer("app-1", true, "cand-1", "JOB_SEEKER"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HIRED");
    }

    @Test
    void respondToOffer_notOwner_throwsForbidden() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> applicationService.respondToOffer("app-1", true, "other-user", "JOB_SEEKER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void respondToOffer_admin_canRespondForAnyone() {
        app.setStatus(JobApplication.Status.HIRED);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        doNothing().when(eventPublisher).publishStatusChanged(any());

        JobApplication result = applicationService.respondToOffer("app-1", true, "admin-1", "ADMIN");

        assertThat(result.getStatus()).isEqualTo(JobApplication.Status.OFFER_ACCEPTED);
    }

    @Test
    void respondToOffer_notFound_throwsResourceNotFound() {
        when(applicationRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.respondToOffer("bad-id", true, "cand-1", "JOB_SEEKER"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getByJobId ────────────────────────────────────────────────────────────

    @Test
    void getByJobId_admin_returnsAll() {
        when(applicationRepository.findByJobId("job-1")).thenReturn(List.of(app));

        List<JobApplication> result = applicationService.getByJobId("job-1", "admin-1", "ADMIN");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByJobId_recruiterOwner_withStoredRecruiterId_returnsApps() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn("rec-1");
        when(applicationRepository.findByJobId("job-1")).thenReturn(List.of(app));

        List<JobApplication> result = applicationService.getByJobId("job-1", "rec-1", "RECRUITER");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByJobId_recruiterNotOwner_withStoredRecruiterId_throwsForbidden() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn("rec-1");

        assertThatThrownBy(() -> applicationService.getByJobId("job-1", "other-rec", "RECRUITER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getByJobId_noStoredRecruiterId_fetchesFromJobService() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn(null);
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));
        when(applicationRepository.findByJobId("job-1")).thenReturn(List.of(app));

        List<JobApplication> result = applicationService.getByJobId("job-1", "rec-1", "RECRUITER");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByJobId_blankStoredRecruiterId_fetchesFromJobService() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn(" ");
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));
        when(applicationRepository.findByJobId("job-1")).thenReturn(List.of(app));

        List<JobApplication> result = applicationService.getByJobId("job-1", "rec-1", "RECRUITER");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByJobId_missingJobData_throwsResourceNotFound() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn(null);
        when(jobServiceClient.getJobById("job-1")).thenReturn(Map.of());

        assertThatThrownBy(() -> applicationService.getByJobId("job-1", "rec-1", "RECRUITER"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found: job-1");
    }

    @Test
    void getByJobId_jobServiceRecruiterMismatch_throwsForbidden() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn(null);
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));

        assertThatThrownBy(() -> applicationService.getByJobId("job-1", "other-rec", "RECRUITER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getByJobId_jobServiceUnavailable_throwsResourceNotFound() {
        when(applicationRepository.findRecruiterIdByJobId("job-1")).thenReturn(null);
        when(jobServiceClient.getJobById("job-1")).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> applicationService.getByJobId("job-1", "rec-1", "RECRUITER"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateStatus with stored recruiterId ──────────────────────────────────

    @Test
    void updateStatus_withStoredRecruiterId_success() {
        app.setRecruiterId("rec-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        doNothing().when(eventPublisher).publishStatusChanged(any());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        JobApplication result = applicationService.updateStatus("app-1", req, "rec-1", "RECRUITER");

        assertThat(result).isNotNull();
    }

    @Test
    void updateStatus_admin_canUpdateAny() {
        app.setRecruiterId("rec-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        doNothing().when(eventPublisher).publishStatusChanged(any());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.REJECTED);

        JobApplication result = applicationService.updateStatus("app-1", req, "admin-1", "ADMIN");

        assertThat(result).isNotNull();
    }

    @Test
    void updateStatus_withoutStoredRecruiterId_fetchesJobAndStoresRecruiter() {
        app.setRecruiterId(null);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));
        when(applicationRepository.save(any())).thenReturn(app);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        JobApplication result = applicationService.updateStatus("app-1", req, "rec-1", "RECRUITER");

        assertThat(result.getRecruiterId()).isEqualTo("rec-1");
    }

    @Test
    void updateStatus_withBlankStoredRecruiterId_fetchesJob() {
        app.setRecruiterId(" ");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));
        when(applicationRepository.save(any())).thenReturn(app);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        JobApplication result = applicationService.updateStatus("app-1", req, "rec-1", "RECRUITER");

        assertThat(result.getRecruiterId()).isEqualTo("rec-1");
    }

    @Test
    void updateStatus_withoutStoredRecruiterId_jobServiceUnavailable_throwsResourceNotFound() {
        app.setRecruiterId(null);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(jobServiceClient.getJobById("job-1")).thenThrow(new RuntimeException("down"));

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        assertThatThrownBy(() -> applicationService.updateStatus("app-1", req, "rec-1", "RECRUITER"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_withoutStoredRecruiterId_missingJobData_throwsResourceNotFound() {
        app.setRecruiterId(null);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(jobServiceClient.getJobById("job-1")).thenReturn(Map.of());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        assertThatThrownBy(() -> applicationService.updateStatus("app-1", req, "rec-1", "RECRUITER"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found: job-1");
    }

    @Test
    void updateStatus_withoutStoredRecruiterId_recruiterMismatch_throwsForbidden() {
        app.setRecruiterId(null);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(jobServiceClient.getJobById("job-1"))
                .thenReturn(Map.of("data", Map.of("recruiterId", "rec-1")));

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        assertThatThrownBy(() -> applicationService.updateStatus("app-1", req, "other-rec", "RECRUITER"))
                .isInstanceOf(ForbiddenException.class);
    }
}
