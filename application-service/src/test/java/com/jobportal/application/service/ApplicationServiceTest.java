package com.jobportal.application.service;

import com.jobportal.application.client.JobServiceClient;
import com.jobportal.application.client.UserServiceClient;
import com.jobportal.application.dto.ApplyRequest;
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
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock JobServiceClient jobServiceClient;
    @Mock UserServiceClient userServiceClient;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks ApplicationService applicationService;

    private ApplyRequest applyReq;
    private JobApplication savedApp;

    @BeforeEach
    void setUp() {
        applyReq = new ApplyRequest();
        applyReq.setJobId("job-1");
        applyReq.setCandidateId("user-1");

        savedApp = new JobApplication();
        savedApp.setId("app-1");
        savedApp.setJobId("job-1");
        savedApp.setCandidateId("user-1");
        savedApp.setJobTitle("Backend Developer");
        savedApp.setStatus(JobApplication.Status.APPLIED);
    }

    @Test
    void apply_success() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "OPEN")));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", "user-1")).thenReturn(false);
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("email", "user@example.com")));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(savedApp);
        doNothing().when(eventPublisher).publishJobApplied(any());

        JobApplication result = applicationService.apply(applyReq);

        assertThat(result.getJobId()).isEqualTo("job-1");
        verify(applicationRepository).save(any(JobApplication.class));
    }

    @Test
    void apply_closedJob_throwsBadRequest() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "CLOSED")));

        assertThatThrownBy(() -> applicationService.apply(applyReq))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Cannot apply for a closed job");
    }

    @Test
    void apply_duplicate_throwsBadRequest() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "OPEN")));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(applyReq))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Already applied for this job");
    }

    @Test
    void apply_jobServiceUnavailable_throwsResourceNotFound() {
        when(jobServiceClient.getJobById("job-1")).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> applicationService.apply(applyReq))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void apply_missingJobData_throwsResourceNotFound() {
        when(jobServiceClient.getJobById("job-1")).thenReturn(Map.of());

        assertThatThrownBy(() -> applicationService.apply(applyReq))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Job not found: job-1");
    }

    @Test
    void apply_nonJobSeekerCandidate_throwsForbidden() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "OPEN")));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", "user-1")).thenReturn(false);
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("email", "recruiter@example.com", "role", "RECRUITER")));

        assertThatThrownBy(() -> applicationService.apply(applyReq))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Only job seekers can apply for jobs");
    }

    @Test
    void apply_missingUserData_continuesWithoutEmail() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "OPEN", "recruiterId", "rec-1")));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", "user-1")).thenReturn(false);
        when(userServiceClient.getUserById("user-1")).thenReturn(Map.of());
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            app.setId("app-1");
            return app;
        });

        JobApplication result = applicationService.apply(applyReq);

        assertThat(result.getCandidateEmail()).isNull();
        assertThat(result.getRecruiterId()).isEqualTo("rec-1");
    }

    @Test
    void apply_userServiceUnavailable_continuesWithoutEmail() {
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("title", "Backend Developer", "status", "OPEN")));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", "user-1")).thenReturn(false);
        when(userServiceClient.getUserById("user-1")).thenThrow(new RuntimeException("down"));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            app.setId("app-1");
            return app;
        });

        JobApplication result = applicationService.apply(applyReq);

        assertThat(result.getCandidateEmail()).isNull();
    }

    @Test
    void getByCandidateId_ownApplications_success() {
        when(applicationRepository.findByCandidateId("user-1")).thenReturn(List.of(savedApp));

        List<JobApplication> result = applicationService.getByCandidateId("user-1", "user-1", "JOB_SEEKER");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByCandidateId_otherUser_throwsForbidden() {
        assertThatThrownBy(() -> applicationService.getByCandidateId("user-1", "other-user", "JOB_SEEKER"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getByCandidateId_adminCanViewOtherUser() {
        when(applicationRepository.findByCandidateId("user-1")).thenReturn(List.of(savedApp));

        List<JobApplication> result = applicationService.getByCandidateId("user-1", "admin-1", "ADMIN");

        assertThat(result).hasSize(1);
    }

    @Test
    void updateStatus_notOwner_throwsForbidden() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(savedApp));
        when(jobServiceClient.getJobById("job-1"))
            .thenReturn(Map.of("data", Map.of("recruiterId", "recruiter-1")));

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        assertThatThrownBy(() -> applicationService.updateStatus("app-1", req, "other-recruiter", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateStatus_notFound_throwsResourceNotFound() {
        when(applicationRepository.findById("missing")).thenReturn(Optional.empty());
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        assertThatThrownBy(() -> applicationService.updateStatus("missing", req, "rec-1", "RECRUITER"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
