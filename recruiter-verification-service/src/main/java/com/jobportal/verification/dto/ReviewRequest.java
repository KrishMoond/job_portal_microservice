package com.jobportal.verification.dto;

import com.jobportal.verification.model.ReviewDecision;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(@NotNull ReviewDecision decision, String reason, String reviewerId) {}
