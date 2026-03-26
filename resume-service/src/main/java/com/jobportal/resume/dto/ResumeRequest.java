package com.jobportal.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResumeRequest {

    @NotBlank private String userId;
    @NotBlank
    @Pattern(regexp = "^(https?://).{3,500}$", message = "File URL must be a valid URL starting with http:// or https://")
    private String fileUrl;
    private String fileName;

    public ResumeRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
