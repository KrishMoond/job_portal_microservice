package com.jobportal.verification.dto;

import com.jobportal.verification.model.VerificationStatus;
import java.time.LocalDateTime;
import java.util.List;

public record RecruiterSubmissionDto(
    Long id,
    String companyName,
    String workEmail,
    String emailDomain,
    String companyWebsite,
    String registrationNumber,
    String contactName,
    String phone,
    String documentUrl,
    String documentType,
    VerificationStatus status,
    LocalDateTime submittedAt,
    List<RiskCheckDto> riskChecks,
    List<AuditLogDto> auditLogs
) {}
