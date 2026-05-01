package com.jobportal.user.dto;

import com.jobportal.user.model.User;
import jakarta.validation.constraints.Pattern;

public class UserUpdateRequest {

    @Pattern(regexp = "^[a-zA-Z\\s'-]{2,50}$", message = "Name must be 2-50 characters and contain only letters, spaces, hyphens, or apostrophes")
    private String name;

    private String phone;

    private String location;

    private User.ExperienceLevel experienceLevel;

    private String education;

    private String preferredJobTypes;

    public UserUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public User.ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(User.ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getPreferredJobTypes() { return preferredJobTypes; }
    public void setPreferredJobTypes(String preferredJobTypes) { this.preferredJobTypes = preferredJobTypes; }
}