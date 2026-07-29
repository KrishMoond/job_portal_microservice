package com.jobportal.verification.dto;

import com.jobportal.verification.model.ReviewDecision;
import java.time.LocalDateTime;

public record AuditLogDto(Long id, String reviewerId, ReviewDecision decision, String reason, LocalDateTime reviewedAt) {}
