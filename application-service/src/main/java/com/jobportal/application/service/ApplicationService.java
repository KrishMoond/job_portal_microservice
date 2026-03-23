package com.jobportal.application.service;

import com.jobportal.application.client.JobServiceClient;
import com.jobportal.application.client.UserServiceClient;
import com.jobportal.application.dto.ApplyRequest;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.messaging.ApplicationEventPublisher;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.common.events.JobAppliedEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

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

    @SuppressWarnings("unchecked")
    public JobApplication apply(ApplyRequest req) {
        if (applicationRepository.existsByJobIdAndCandidateId(req.getJobId(), req.getCandidateId()))
            throw new BadRequestException("Already applied for this job");

        Map<String, Object> jobResp = jobServiceClient.getJobById(req.getJobId());
        Map<String, Object> jobData = (Map<String, Object>) jobResp.get("data");
        if (jobData == null) throw new ResourceNotFoundException("Job not found: " + req.getJobId());
        String jobTitle = (String) jobData.get("title");

        Map<String, Object> userResp = userServiceClient.getUserById(req.getCandidateId());
        Map<String, Object> userData = (Map<String, Object>) userResp.get("data");
        if (userData == null) throw new ResourceNotFoundException("User not found: " + req.getCandidateId());
        String candidateEmail = (String) userData.get("email");

        JobApplication application = new JobApplication();
        application.setJobId(req.getJobId());
        application.setJobTitle(jobTitle);
        application.setCandidateId(req.getCandidateId());
        application.setCandidateEmail(candidateEmail);
        application.setResumeId(req.getResumeId());
        application = applicationRepository.save(application);

        JobAppliedEvent event = new JobAppliedEvent(
            application.getId(), req.getJobId(), jobTitle,
            req.getCandidateId(), candidateEmail);
        eventPublisher.publishJobApplied(event);

        return application;
    }

    public List<JobApplication> getByJobId(String jobId, String requesterId, String role) {
        if (!"ADMIN".equals(role)) {
            // recruiter can only see applications for their own jobs
            Map<String, Object> jobResp = jobServiceClient.getJobById(jobId);
            Map<String, Object> jobData = (Map<String, Object>) jobResp.get("data");
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
            // recruiter can only update status for applications on their own jobs
            Map<String, Object> jobResp = jobServiceClient.getJobById(app.getJobId());
            Map<String, Object> jobData = (Map<String, Object>) jobResp.get("data");
            if (jobData == null) throw new ResourceNotFoundException("Job not found: " + app.getJobId());
            String recruiterId = (String) jobData.get("recruiterId");
            if (!recruiterId.equals(requesterId))
                throw new ForbiddenException("You can only update status for applications on your own jobs");
        }
        app.setStatus(req.getStatus());
        return applicationRepository.save(app);
    }
}
