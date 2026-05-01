package com.jobportal.common.events;

import java.io.Serializable;

public class JobAppliedEvent implements Serializable {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String candidateId;
    private String candidateEmail;
    private String recruiterId;

    public JobAppliedEvent() {}

    public JobAppliedEvent(String applicationId, String jobId, String jobTitle, String candidateId, String candidateEmail) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
    }

    public JobAppliedEvent(String applicationId, String jobId, String jobTitle, String candidateId, String candidateEmail, String recruiterId) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.recruiterId = recruiterId;
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
}
