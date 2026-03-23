package com.jobportal.common.events;

import java.io.Serializable;

public class ResumeUploadedEvent implements Serializable {
    private String resumeId;
    private String userId;
    private String fileUrl;

    public ResumeUploadedEvent() {}

    public ResumeUploadedEvent(String resumeId, String userId, String fileUrl) {
        this.resumeId = resumeId;
        this.userId = userId;
        this.fileUrl = fileUrl;
    }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
