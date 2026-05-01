package com.jobportal.user.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;
    private String location;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @Column(columnDefinition = "TEXT")
    private String education;

    private String preferredJobTypes; // comma-separated

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role { JOB_SEEKER, RECRUITER, ADMIN }
    public enum ExperienceLevel { ENTRY, JUNIOR, MID, SENIOR, EXECUTIVE }

    public User() {}

    public User(String id, String name, String email, String password, Role role,
                String phone, String location, ExperienceLevel experienceLevel,
                String education, String preferredJobTypes,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.name = name; this.email = email;
        this.password = password; this.role = role;
        this.phone = phone; this.location = location;
        this.experienceLevel = experienceLevel;
        this.education = education; this.preferredJobTypes = preferredJobTypes;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getPreferredJobTypes() { return preferredJobTypes; }
    public void setPreferredJobTypes(String preferredJobTypes) { this.preferredJobTypes = preferredJobTypes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
