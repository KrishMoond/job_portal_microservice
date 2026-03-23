package com.jobportal.job.dto;

import com.jobportal.job.model.Job;
import java.time.LocalDateTime;

public class JobResponse {
    private String jobId;
    private String title;
    private String company;
    private String location;
    private String salary;
    private String description;
    private String recruiterId;
    private Job.Status status;
    private LocalDateTime createdAt;

    public JobResponse() {}

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
    public Job.Status getStatus() { return status; }
    public void setStatus(Job.Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
