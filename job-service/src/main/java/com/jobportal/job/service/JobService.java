package com.jobportal.job.service;

import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.dto.JobResponse;
import com.jobportal.job.messaging.JobEventPublisher;
import com.jobportal.job.model.Job;
import com.jobportal.job.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventPublisher eventPublisher;

    public JobService(JobRepository jobRepository, JobEventPublisher eventPublisher) {
        this.jobRepository = jobRepository;
        this.eventPublisher = eventPublisher;
    }

    public JobResponse createJob(JobRequest req, String recruiterId) {
        Job job = new Job();
        job.setTitle(req.getTitle());
        job.setCompany(req.getCompany());
        job.setLocation(req.getLocation());
        job.setSalary(req.getSalary());
        job.setDescription(req.getDescription());
        job.setRecruiterId(recruiterId);
        job = jobRepository.save(job);

        JobCreatedEvent event = new JobCreatedEvent(
            job.getId(), job.getTitle(), job.getCompany(), job.getLocation(), recruiterId);
        eventPublisher.publishJobCreated(event);

        return toResponse(job);
    }

    public JobResponse getById(String jobId) {
        return toResponse(jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId)));
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public JobResponse closeJob(String jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
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
