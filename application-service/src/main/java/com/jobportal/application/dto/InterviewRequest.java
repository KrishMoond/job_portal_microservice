package com.jobportal.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class InterviewRequest {

    @NotBlank private String applicationId;
    @NotBlank private String candidateId;
    @NotNull  private LocalDateTime scheduledAt;
    @Pattern(regexp = "^(https?://).{3,200}$", message = "Meeting link must be a valid URL starting with http:// or https://")
    private String meetingLink;

    public InterviewRequest() {}

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
}
