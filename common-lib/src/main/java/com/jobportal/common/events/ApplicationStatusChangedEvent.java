package com.jobportal.common.events;

import java.io.Serializable;

public class ApplicationStatusChangedEvent implements Serializable {

    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String candidateId;
    private String candidateEmail;
    private String newStatus;
    private String recruiterId;

    public ApplicationStatusChangedEvent() {}

    public ApplicationStatusChangedEvent(String applicationId, String jobId, String jobTitle,
                                          String candidateId, String candidateEmail,
                                          String newStatus, String recruiterId) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.newStatus = newStatus;
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
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
}
