package com.jobportal.common.events;

import java.io.Serializable;

public class JobCreatedEvent implements Serializable {
    private String jobId;
    private String title;
    private String company;
    private String location;
    private String recruiterId;

    public JobCreatedEvent() {}

    public JobCreatedEvent(String jobId, String title, String company, String location, String recruiterId) {
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.recruiterId = recruiterId;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
}
