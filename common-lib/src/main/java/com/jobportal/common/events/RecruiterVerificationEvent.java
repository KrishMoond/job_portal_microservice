package com.jobportal.common.events;

import java.io.Serializable;

public class RecruiterVerificationEvent implements Serializable {

    private String submissionId;
    private String recruiterId;   // userId of the recruiter (workEmail used as fallback)
    private String recruiterEmail;
    private String companyName;
    private String status;        // PENDING, UNDER_REVIEW, VERIFIED, REJECTED, MORE_INFO_REQUESTED

    public RecruiterVerificationEvent() {}

    public RecruiterVerificationEvent(String submissionId, String recruiterId,
                                      String recruiterEmail, String companyName, String status) {
        this.submissionId = submissionId;
        this.recruiterId = recruiterId;
        this.recruiterEmail = recruiterEmail;
        this.companyName = companyName;
        this.status = status;
    }

    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
