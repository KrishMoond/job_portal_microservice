package com.jobportal.application.service;

import com.jobportal.application.client.JobServiceClient;
import com.jobportal.application.client.UserServiceClient;
import com.jobportal.application.dto.ApplyRequest;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.messaging.ApplicationEventPublisher;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.common.events.ApplicationStatusChangedEvent;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobServiceClient jobServiceClient;
    private final UserServiceClient userServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationService(ApplicationRepository applicationRepository,
                               JobServiceClient jobServiceClient,
                               UserServiceClient userServiceClient,
                               ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.jobServiceClient = jobServiceClient;
        this.userServiceClient = userServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @org.springframework.transaction.annotation.Transactional
    @SuppressWarnings("unchecked")
    public JobApplication apply(ApplyRequest req) {
        // 1. Fetch job via Feign — fail fast if job not found
        Map<String, Object> jobData = null;
        try {
            Map<String, Object> jobResp = jobServiceClient.getJobById(req.getJobId());
            jobData = (Map<String, Object>) jobResp.get("data");
        } catch (Exception e) {
            throw new ResourceNotFoundException("Job not found or job-service unavailable: " + req.getJobId());
        }
        if (jobData == null) throw new ResourceNotFoundException("Job not found: " + req.getJobId());

        // 2. Check job status
        String jobStatus = (String) jobData.get("status");
        if ("CLOSED".equals(jobStatus))
            throw new BadRequestException("Cannot apply for a closed job");

        // 3. Check duplicate
        if (applicationRepository.existsByJobIdAndCandidateId(req.getJobId(), req.getCandidateId()))
            throw new BadRequestException("Already applied for this job");

        String jobTitle = (String) jobData.get("title");

        // 4. Fetch candidate details and enforce JOB_SEEKER role
        String candidateEmail = null;
        try {
            Map<String, Object> userResp = userServiceClient.getUserById(req.getCandidateId());
            Map<String, Object> userData = (Map<String, Object>) userResp.get("data");
            if (userData != null) {
                candidateEmail = (String) userData.get("email");
                String candidateRole = (String) userData.get("role");
                if (candidateRole != null && !"JOB_SEEKER".equals(candidateRole)) {
                    throw new ForbiddenException("Only job seekers can apply for jobs");
                }
            }
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[FEIGN] user-service unavailable for candidateId={}, proceeding without email", req.getCandidateId(), e);
        }

        // 5. Save and publish
        JobApplication application = new JobApplication();
        application.setJobId(req.getJobId());
        application.setJobTitle(jobTitle);
        String recruiterId = (String) jobData.get("recruiterId");
        application.setRecruiterId(recruiterId);
        application.setCandidateId(req.getCandidateId());
        application.setCandidateEmail(candidateEmail);
        application.setResumeId(req.getResumeId());
        application = applicationRepository.save(application);

        eventPublisher.publishJobApplied(new JobAppliedEvent(
            application.getId(), req.getJobId(), jobTitle,
            req.getCandidateId(), candidateEmail, recruiterId));

        return application;
    }

    public List<JobApplication> getByJobId(String jobId, String requesterId, String role) {
        if (!"ADMIN".equals(role)) {
            // Fast-path: if we already have applications, validate recruiter ownership without calling job-service.
            String storedRecruiterId = applicationRepository.findRecruiterIdByJobId(jobId);
            if (storedRecruiterId != null && !storedRecruiterId.isBlank()) {
                if (!storedRecruiterId.equals(requesterId))
                    throw new ForbiddenException("You can only view applications for your own jobs");
                return applicationRepository.findByJobId(jobId);
            }

            Map<String, Object> jobData = null;
            try {
                Map<String, Object> jobResp = jobServiceClient.getJobById(jobId);
                jobData = (Map<String, Object>) jobResp.get("data");
            } catch (Exception e) {
                throw new ResourceNotFoundException("Job not found or job-service unavailable: " + jobId);
            }
            if (jobData == null) throw new ResourceNotFoundException("Job not found: " + jobId);
            String recruiterId = (String) jobData.get("recruiterId");
            if (!recruiterId.equals(requesterId))
                throw new ForbiddenException("You can only view applications for your own jobs");
        }
        return applicationRepository.findByJobId(jobId);
    }

    public List<JobApplication> getByCandidateId(String candidateId, String requesterId, String role) {
        // job seeker can only see their own applications
        if (!"ADMIN".equals(role) && !candidateId.equals(requesterId))
            throw new ForbiddenException("You can only view your own applications");
        return applicationRepository.findByCandidateId(candidateId);
    }

    public JobApplication updateStatus(String applicationId, StatusUpdateRequest req, String requesterId, String role) {
        JobApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!"ADMIN".equals(role)) {
            // Prefer stored recruiterId (avoid job-service call)
            if (app.getRecruiterId() != null && !app.getRecruiterId().isBlank()) {
                if (!app.getRecruiterId().equals(requesterId))
                    throw new ForbiddenException("You can only update status for applications on your own jobs");
            } else {
                Map<String, Object> jobData = null;
                try {
                    Map<String, Object> jobResp = jobServiceClient.getJobById(app.getJobId());
                    jobData = (Map<String, Object>) jobResp.get("data");
                } catch (Exception e) {
                    throw new ResourceNotFoundException("Job not found or job-service unavailable: " + app.getJobId());
                }
                if (jobData == null) throw new ResourceNotFoundException("Job not found: " + app.getJobId());
                String recruiterId = (String) jobData.get("recruiterId");
                if (!recruiterId.equals(requesterId))
                    throw new ForbiddenException("You can only update status for applications on your own jobs");
                app.setRecruiterId(recruiterId);
            }
        }
        app.setStatus(req.getStatus());
        app = applicationRepository.save(app);
        // Notify candidate of every status change
        eventPublisher.publishStatusChanged(new ApplicationStatusChangedEvent(
            app.getId(), app.getJobId(), app.getJobTitle(),
            app.getCandidateId(), app.getCandidateEmail(),
            req.getStatus().name(), app.getRecruiterId()));
        return app;
    }

    @org.springframework.transaction.annotation.Transactional
    public JobApplication respondToOffer(String applicationId, boolean accepted, String requesterId, String role) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!"ADMIN".equals(role) && !app.getCandidateId().equals(requesterId)) {
            throw new ForbiddenException("You can only respond to your own offer");
        }
        if (app.getStatus() != JobApplication.Status.HIRED) {
            throw new BadRequestException("Offer response is only allowed after status is HIRED");
        }

        app.setStatus(accepted ? JobApplication.Status.OFFER_ACCEPTED : JobApplication.Status.OFFER_REJECTED);
        app = applicationRepository.save(app);
        // Notify candidate of offer response confirmation
        eventPublisher.publishStatusChanged(new ApplicationStatusChangedEvent(
            app.getId(), app.getJobId(), app.getJobTitle(),
            app.getCandidateId(), app.getCandidateEmail(),
            app.getStatus().name(), app.getRecruiterId()));
        return app;
    }
}
