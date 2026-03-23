package com.jobportal.common.events;

import java.io.Serializable;

public class JobClosedEvent implements Serializable {
    private String jobId;
    private String title;
    private String recruiterId;

    public JobClosedEvent() {}

    public JobClosedEvent(String jobId, String title, String recruiterId) {
        this.jobId = jobId;
        this.title = title;
        this.recruiterId = recruiterId;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
}
