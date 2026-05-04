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

    @Test
    void closeJob_adminCanCloseWithoutRequesterId() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        JobResponse response = jobService.closeJob("job-1", null, "ADMIN");

        assertThat(response.getStatus()).isEqualTo(Job.Status.CLOSED);
        verify(eventPublisher).publishJobClosed(any());
    }

    @Test
    void closeJob_blankRequester_throwsForbidden() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.closeJob("job-1", " ", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Authentication required to close job");
    }

    @Test
    void closeJob_notFound_throwsResourceNotFound() {
        when(jobRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.closeJob("missing", "user-1", "RECRUITER"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reopenJob_success() {
        savedJob.setStatus(Job.Status.CLOSED);
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        JobResponse response = jobService.reopenJob("job-1", "user-1", "RECRUITER");

        assertThat(response.getStatus()).isEqualTo(Job.Status.OPEN);
        verify(eventPublisher).publishJobCreated(any());
    }

    @Test
    void reopenJob_adminCanReopenWithoutRequesterId() {
        savedJob.setStatus(Job.Status.CLOSED);
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        JobResponse response = jobService.reopenJob("job-1", null, "ADMIN");

        assertThat(response.getStatus()).isEqualTo(Job.Status.OPEN);
    }

    @Test
    void reopenJob_blankRequester_throwsForbidden() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.reopenJob("job-1", " ", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Authentication required to reopen job");
    }

    @Test
    void reopenJob_notOwner_throwsForbidden() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.reopenJob("job-1", "other-user", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void reopenJob_alreadyOpen_throwsBadRequest() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(savedJob));

        assertThatThrownBy(() -> jobService.reopenJob("job-1", "user-1", "RECRUITER"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Job is already open");
    }

    @Test
    void reopenJob_notFound_throwsResourceNotFound() {
        when(jobRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.reopenJob("missing", "user-1", "RECRUITER"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createJob_userServiceUnavailable_allowsRecruiterTokenRole() {
        when(userServiceClient.getUserById("user-1")).thenThrow(new RuntimeException("down"));
        when(userServiceClient.getCompanyById("company-1")).thenReturn(Map.of("name", "Tech Corp"));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId("job-1");
            return job;
        });

        JobResponse response = jobService.createJob(req, "user-1", "RECRUITER");

        assertThat(response.getJobId()).isEqualTo("job-1");
    }

    @Test
    void createJob_userServiceUnavailable_rejectsJobSeekerTokenRole() {
        when(userServiceClient.getUserById("user-1")).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> jobService.createJob(req, "user-1", "JOB_SEEKER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Only recruiters and admins can post jobs");
    }

    @Test
    void createJob_userDataMissing_throwsForbidden() {
        when(userServiceClient.getUserById("user-1")).thenReturn(Map.of());

        assertThatThrownBy(() -> jobService.createJob(req, "user-1", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("User not found: user-1");
    }

    @Test
    void createJob_roleMismatch_throwsForbidden() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "ADMIN")));

        assertThatThrownBy(() -> jobService.createJob(req, "user-1", "RECRUITER"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("Role mismatch");
    }

    @Test
    void createJob_companyLookupFallsBackToCompanyId() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "RECRUITER")));
        when(userServiceClient.getCompanyById("company-1")).thenThrow(new RuntimeException("down"));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId("job-1");
            return job;
        });

        JobResponse response = jobService.createJob(req, "user-1", "RECRUITER");

        assertThat(response.getCompany()).isEqualTo("company-1");
    }

    @Test
    void createJob_companyNotFound_throwsResourceNotFound() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "RECRUITER")));
        when(userServiceClient.getCompanyById("company-1")).thenReturn(null);

        assertThatThrownBy(() -> jobService.createJob(req, "user-1", "RECRUITER"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Company not found: company-1");
    }

    @Test
    void createJob_resolvesRequestedAndInferredCategories() {
        when(userServiceClient.getUserById("user-1"))
            .thenReturn(Map.of("data", Map.of("role", "RECRUITER")));
        when(userServiceClient.getCompanyById("company-1")).thenReturn(Map.of("name", "Tech Corp"));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId("job-1");
            return job;
        });

        req.setCategory("  Custom  ");
        assertThat(jobService.createJob(req, "user-1", "RECRUITER").getCategory()).isEqualTo("Custom");

        req.setCategory(null);
        assertCategory("Design", "UX Designer", "Figma visual systems");
        assertCategory("Marketing", "SEO Lead", "brand campaign");
        assertCategory("Finance", "Auditor", "tax accounting");
        assertCategory("Healthcare", "Nurse", "clinical medical role");
        assertCategory("Support", "Helpdesk", "customer success");
        assertCategory("Content", "Copywriter", "editor content");
        assertCategory("Business", "Business Analyst", "operations strategy");
        assertCategory("Intern", "Graduate Trainee", "internship");
        assertCategory("Engineering", null, null);
    }

    private void assertCategory(String expected, String title, String description) {
        req.setCategory(null);
        req.setTitle(title);
        req.setDescription(description);

        JobResponse response = jobService.createJob(req, "user-1", "RECRUITER");

        assertThat(response.getCategory()).isEqualTo(expected);
    }
}
