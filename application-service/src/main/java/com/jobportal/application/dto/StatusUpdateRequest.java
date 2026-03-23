package com.jobportal.application.dto;

import com.jobportal.application.model.JobApplication;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull
    private JobApplication.Status status;

    public StatusUpdateRequest() {}

    public JobApplication.Status getStatus() { return status; }
    public void setStatus(JobApplication.Status status) { this.status = status; }
}
