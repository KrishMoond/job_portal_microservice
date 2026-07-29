package com.jobportal.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateRecruiterSubmissionRequest(
    @NotBlank String companyName,
    @NotBlank @Email String workEmail,
    @NotBlank String companyWebsite,
    @NotBlank String registrationNumber,
    @NotBlank String contactName,
    String phone,
    @NotBlank String documentUrl,
    @NotBlank String documentType,
    String recruiterId
) {}
