package com.jobportal.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ApplyRequest {

    @NotBlank private String jobId;
    @NotBlank private String candidateId;
    @NotBlank private String resumeId;

    public ApplyRequest() {}

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
}
