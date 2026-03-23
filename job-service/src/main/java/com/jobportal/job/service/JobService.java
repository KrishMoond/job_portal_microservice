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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventPublisher eventPublisher;
    private final UserServiceClient userServiceClient;

    public JobService(JobRepository jobRepository, JobEventPublisher eventPublisher,
                      UserServiceClient userServiceClient) {
        this.jobRepository = jobRepository;
        this.eventPublisher = eventPublisher;
        this.userServiceClient = userServiceClient;
    }

    @SuppressWarnings("unchecked")
    public JobResponse createJob(JobRequest req, String userId) {
        // Verify role directly from user-service DB — never trust client-supplied role
        Map<String, Object> userResp = userServiceClient.getUserById(userId);
        Map<String, Object> userData = (Map<String, Object>) userResp.get("data");
        if (userData == null)
            throw new ResourceNotFoundException("User not found: " + userId);
        String role = (String) userData.get("role");
        if (!"RECRUITER".equals(role) && !"ADMIN".equals(role))
            throw new ForbiddenException("Only recruiters and admins can post jobs");

        Job job = new Job();
        job.setTitle(req.getTitle());
        job.setCompany(req.getCompany());
        job.setLocation(req.getLocation());
        job.setSalary(req.getSalary());
        job.setDescription(req.getDescription());
        job.setRecruiterId(userId);
        job = jobRepository.save(job);

        eventPublisher.publishJobCreated(new JobCreatedEvent(
            job.getId(), job.getTitle(), job.getCompany(), job.getLocation(), userId));

        return toResponse(job);
    }

    public JobResponse getById(String jobId) {
        return toResponse(jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId)));
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public JobResponse closeJob(String jobId, String requesterId, String role) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (!"ADMIN".equals(role) && !job.getRecruiterId().equals(requesterId))
            throw new ForbiddenException("You can only close your own jobs");
        if (job.getStatus() == Job.Status.CLOSED)
            throw new BadRequestException("Job is already closed");
        job.setStatus(Job.Status.CLOSED);
        job = jobRepository.save(job);
        JobClosedEvent event = new JobClosedEvent(job.getId(), job.getTitle(), job.getRecruiterId());
        eventPublisher.publishJobClosed(event);
        return toResponse(job);
    }

    private JobResponse toResponse(Job j) {
        JobResponse r = new JobResponse();
        r.setJobId(j.getId());
        r.setTitle(j.getTitle());
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
