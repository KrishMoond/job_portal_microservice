package com.jobportal.job.service;

import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.job.client.UserServiceClient;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.dto.JobResponse;
import com.jobportal.job.messaging.JobEventPublisher;
import com.jobportal.job.model.Job;
import com.jobportal.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobEventPublisher eventPublisher;
    private final UserServiceClient userServiceClient;

    public JobService(JobRepository jobRepository, JobEventPublisher eventPublisher,
                      UserServiceClient userServiceClient) {
        this.jobRepository = jobRepository;
        this.eventPublisher = eventPublisher;
        this.userServiceClient = userServiceClient;
    }

    @org.springframework.transaction.annotation.Transactional
    public JobResponse createJob(JobRequest req, String userId, String roleFromToken) {
        verifyRoleFromDb(userId, roleFromToken);

        Job job = new Job();
        job.setTitle(req.getTitle());
        job.setCompanyId(req.getCompanyId());
        job.setCompany(resolveCompanyName(req.getCompanyId()));
        job.setLocation(req.getLocation());
        job.setSalary(req.getSalary());
        job.setDescription(req.getDescription());
        job.setRecruiterId(userId);
        job = jobRepository.save(job);

        publishJobCreated(job, userId);
        return toResponse(job);
    }

    public JobResponse getById(String jobId) {
        return toResponse(jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId)));
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public JobResponse closeJob(String jobId, String requesterId, String role) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        boolean isBlankRequester = requesterId == null || requesterId.isBlank();
        if (!isBlankRequester && !"ADMIN".equals(role) && !requesterId.equals(job.getRecruiterId()))
            throw new ForbiddenException("You can only close your own jobs");
        if (job.getStatus() == Job.Status.CLOSED)
            throw new BadRequestException("Job is already closed");
        job.setStatus(Job.Status.CLOSED);
        job = jobRepository.save(job);
        publishJobClosed(job);
        return toResponse(job);
    }

    @SuppressWarnings("unchecked")
    private void verifyRoleFromDb(String userId, String roleFromToken) {
        if (userId == null || userId.isBlank())
            throw new ForbiddenException("Authentication required");
        try {
            Map<String, Object> resp = userServiceClient.getUserById(userId);
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null)
                throw new ForbiddenException("User not found: " + userId);
            String dbRole = (String) data.get("role");
            if (!dbRole.equals(roleFromToken))
                throw new ForbiddenException("Role mismatch — token role does not match DB role");
            if (!"RECRUITER".equals(dbRole) && !"ADMIN".equals(dbRole))
                throw new ForbiddenException("Only recruiters and admins can post jobs");
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Feign] user-service unavailable for userId={}, falling back to token role", userId);
            if (!"RECRUITER".equals(roleFromToken) && !"ADMIN".equals(roleFromToken))
                throw new ForbiddenException("Only recruiters and admins can post jobs");
        }
    }

    private String resolveCompanyName(String companyId) {
        try {
            Map<String, Object> companyResp = userServiceClient.getCompanyById(companyId);
            if (companyResp == null)
                throw new ResourceNotFoundException("Company not found: " + companyId);
            return (String) companyResp.get("name");
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Feign] Could not resolve company name for companyId={}, using id as fallback", companyId, e);
            return companyId;
        }
    }

    private void publishJobCreated(Job job, String userId) {
        eventPublisher.publishJobCreated(new JobCreatedEvent(
            job.getId(), job.getTitle(), job.getCompany(), job.getLocation(),
            job.getSalary(), job.getDescription(), userId));
    }

    private void publishJobClosed(Job job) {
        eventPublisher.publishJobClosed(new JobClosedEvent(job.getId(), job.getTitle(), job.getRecruiterId()));
    }

    private JobResponse toResponse(Job j) {
        JobResponse r = new JobResponse();
        r.setJobId(j.getId());
        r.setTitle(j.getTitle());
        r.setCompanyId(j.getCompanyId());
        r.setCompany(j.getCompany());
        r.setLocation(j.getLocation());
        r.setSalary(j.getSalary());
        r.setDescription(j.getDescription());
        r.setRecruiterId(j.getRecruiterId());
        r.setStatus(j.getStatus());
        r.setCreatedAt(j.getCreatedAt());
        return r;
    }
}
