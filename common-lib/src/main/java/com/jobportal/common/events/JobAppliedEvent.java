package com.jobportal.common.events;

import java.io.Serializable;

public class JobAppliedEvent implements Serializable {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String candidateId;
    private String candidateEmail;

    public JobAppliedEvent() {}

    public JobAppliedEvent(String applicationId, String jobId, String jobTitle, String candidateId, String candidateEmail) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
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
}
