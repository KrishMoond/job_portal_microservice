package com.jobportal.job.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.job.client.UserServiceClient;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.dto.JobResponse;
import com.jobportal.job.messaging.JobEventPublisher;
import com.jobportal.job.model.Job;
import com.jobportal.job.repository.JobRepository;
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
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock JobEventPublisher eventPublisher;
    @Mock UserServiceClient userServiceClient;
    @InjectMocks JobService jobService;

    private JobRequest req;
    private Job savedJob;

    @BeforeEach
    void setUp() {
        req = new JobRequest();
        req.setTitle("Backend Developer");
        req.setCompanyId("company-1");
        req.setLocation("New York, NY");
        req.setSalary("$80,000/yr");
        req.setDescription("Java role");

        savedJob = new Job();
        savedJob.setId("job-1");
        savedJob.setTitle("Backend Developer");
        savedJob.setCompany("Tech Corp");
        savedJob.setCompanyId("company-1");
        savedJob.setLocation("New York, NY");
        savedJob.setSalary("$80,000/yr");
        savedJob.setRecruiterId("user-1");
        savedJob.setStatus(Job.Status.OPEN);
    }

    @Test
    void createJob_success() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "RECRUITER")));
        when(userServiceClient.getCompanyById("company-1"))
            .thenReturn(Map.of("name", "Tech Corp"));
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        doNothing().when(eventPublisher).publishJobCreated(any());

        JobResponse response = jobService.createJob(req, "user-1", "RECRUITER");

        assertThat(response.getTitle()).isEqualTo("Backend Developer");
        assertThat(response.getStatus()).isEqualTo(Job.Status.OPEN);
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void createJob_emptyUserId_throwsForbidden() {
        assertThatThrownBy(() -> jobService.createJob(req, "", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Authentication required");
    }

    @Test
    void createJob_wrongRole_throwsForbidden() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "JOB_SEEKER")));

        assertThatThrownBy(() -> jobService.createJob(req, "user-1", "JOB_SEEKER"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_found() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        JobResponse response = jobService.getById("job-1");

        assertThat(response.getJobId()).isEqualTo("job-1");
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(jobRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getById("bad-id"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllJobs_returnsList() {
        when(jobRepository.findAll()).thenReturn(List.of(savedJob));

        List<JobResponse> jobs = jobService.getAllJobs();

        assertThat(jobs).hasSize(1);
    }

    @Test
    void closeJob_success() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        doNothing().when(eventPublisher).publishJobClosed(any());

        JobResponse response = jobService.closeJob("job-1", "user-1", "RECRUITER");

        assertThat(response).isNotNull();
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void closeJob_alreadyClosed_throwsBadRequest() {
        savedJob.setStatus(Job.Status.CLOSED);
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.closeJob("job-1", "user-1", "RECRUITER"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Job is already closed");
    }

    @Test
    void closeJob_notOwner_throwsForbidden() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.closeJob("job-1", "other-user", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class);
    }
}
