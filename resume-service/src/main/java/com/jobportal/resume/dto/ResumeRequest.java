package com.jobportal.resume.dto;

import jakarta.validation.constraints.NotBlank;

public class ResumeRequest {

    @NotBlank private String userId;
    @NotBlank private String fileUrl;
    private String fileName;

    public ResumeRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
