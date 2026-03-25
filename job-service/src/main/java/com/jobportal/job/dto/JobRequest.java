package com.jobportal.job.dto;

import jakarta.validation.constraints.NotBlank;

public class JobRequest {

    @NotBlank private String title;
    @NotBlank private String companyId;
    @NotBlank private String location;
    private String salary;
    private String description;

    public JobRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
