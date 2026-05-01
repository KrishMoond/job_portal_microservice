package com.jobportal.user.dto;

import com.jobportal.user.model.User;

public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private User.Role role;
    private String phone;
    private String location;
    private User.ExperienceLevel experienceLevel;
    private String education;
    private String preferredJobTypes;

    public UserResponse() {}

    public UserResponse(String userId, String name, String email, User.Role role,
                       String phone, String location, User.ExperienceLevel experienceLevel,
                       String education, String preferredJobTypes) {
        this.userId = userId; this.name = name;
        this.email = email; this.role = role;
        this.phone = phone; this.location = location;
        this.experienceLevel = experienceLevel;
        this.education = education; this.preferredJobTypes = preferredJobTypes;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
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
