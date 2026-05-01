package com.jobportal.common.events;

import java.io.Serializable;

public class JobCreatedEvent implements Serializable {
    private String jobId;
    private String title;
    private String company;
    private String location;
    private String salary;
    private String description;
    private String recruiterId;
    private String category;

    public JobCreatedEvent() {}

    public JobCreatedEvent(String jobId, String title, String company, String location,
                           String salary, String description, String recruiterId, String category) {
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.description = description;
        this.recruiterId = recruiterId;
        this.category = category;
    }

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
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
