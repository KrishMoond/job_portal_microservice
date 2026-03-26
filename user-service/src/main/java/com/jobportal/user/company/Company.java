package com.jobportal.user.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class Company {

    @Id private String id;
    @Column(nullable = false, unique = true) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Pattern(regexp = "^(https?://).{3,200}$", message = "Website must be a valid URL starting with http:// or https://")
    private String website;
    private String logoUrl;
    @Column(nullable = false) private String createdByUserId;
    private LocalDateTime createdAt;

    public Company() {}

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
