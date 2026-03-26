package com.jobportal.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class JobRequest {

    @NotBlank private String title;
    @NotBlank private String companyId;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s,.-]{2,100}$", message = "Location must be 2-100 characters")
    private String location;
    @Pattern(regexp = "^\\$?\\d{1,3}(,\\d{3})*(\\.\\d{2})?(-\\$?\\d{1,3}(,\\d{3})*(\\.\\d{2})?)?(/yr|/mo|/hr)?$",
             message = "Salary format invalid (e.g. $50,000-$80,000/yr)")
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
