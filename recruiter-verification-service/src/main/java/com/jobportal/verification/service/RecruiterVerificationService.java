package com.jobportal.verification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.RecruiterVerificationEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.verification.config.RabbitMQConfig;
import com.jobportal.verification.dto.*;
import com.jobportal.verification.model.*;
import com.jobportal.verification.outbox.OutboxEvent;
import com.jobportal.verification.outbox.OutboxEventRepository;
import com.jobportal.verification.repository.RecruiterReviewAuditLogRepository;
import com.jobportal.verification.repository.RecruiterSubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@Service
public class RecruiterVerificationService {
    private static final List<VerificationStatus> QUEUE_STATUSES = List.of(VerificationStatus.PENDING, VerificationStatus.UNDER_REVIEW);
    private static final List<VerificationStatus> HISTORY_STATUSES = List.of(VerificationStatus.VERIFIED, VerificationStatus.REJECTED);

    private final RecruiterSubmissionRepository submissions;
    private final RecruiterReviewAuditLogRepository auditLogs;
    private final OutboxEventRepository outboxEvents;
    private final ObjectMapper objectMapper;

    public RecruiterVerificationService(RecruiterSubmissionRepository submissions,
                                        RecruiterReviewAuditLogRepository auditLogs,
                                        OutboxEventRepository outboxEvents,
                                        ObjectMapper objectMapper) {
        this.submissions = submissions;
        this.auditLogs = auditLogs;
        this.outboxEvents = outboxEvents;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RecruiterSubmissionDto createSubmission(CreateRecruiterSubmissionRequest request) {
        RecruiterSubmission submission = new RecruiterSubmission();
        submission.setCompanyName(request.companyName());
        submission.setWorkEmail(request.workEmail());
        submission.setCompanyWebsite(request.companyWebsite());
        submission.setRegistrationNumber(request.registrationNumber());
        submission.setContactName(request.contactName());
        submission.setPhone(request.phone());
        submission.setDocumentUrl(request.documentUrl());
        submission.setDocumentType(request.documentType());
        submission.setRecruiterId(request.recruiterId());
        submission.setStatus(VerificationStatus.PENDING);
        RecruiterSubmission saved = submissions.save(submission);
        publishEvent(saved, VerificationStatus.PENDING.name());
        return toDetailDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<RecruiterSubmissionDto> listQueue(VerificationStatus status, Pageable pageable) {
        if (status != null && !QUEUE_STATUSES.contains(status)) {
            throw new BadRequestException("Queue status must be PENDING or UNDER_REVIEW");
        }
        Page<RecruiterSubmission> page = status == null
            ? submissions.findByStatusIn(QUEUE_STATUSES, pageable)
            : submissions.findByStatus(status, pageable);
        return page.map(this::toSummaryDto);
    }

    @Transactional
    public RecruiterSubmissionDto getDetail(Long id) {
        RecruiterSubmission submission = submissions.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recruiter submission not found"));
        if (submission.getStatus() == VerificationStatus.PENDING) {
            submission.setStatus(VerificationStatus.UNDER_REVIEW);
            submission = submissions.save(submission);
            publishEvent(submission, VerificationStatus.UNDER_REVIEW.name());
        }
        return toDetailDto(submission);
    }

    @Transactional
    public RecruiterSubmissionDto reviewRecruiter(Long id, ReviewDecision decision, String reason, String reviewerId) {
        RecruiterSubmission submission = submissions.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recruiter submission not found"));
        if (!QUEUE_STATUSES.contains(submission.getStatus())) {
            throw new BadRequestException("Only pending or under-review submissions can be reviewed");
        }
        if ((decision == ReviewDecision.REJECT || decision == ReviewDecision.REQUEST_MORE_INFO) && !StringUtils.hasText(reason)) {
            throw new BadRequestException("Reason is required for reject and request-more-info decisions");
        }

        VerificationStatus newStatus = switch (decision) {
            case APPROVE -> VerificationStatus.VERIFIED;
            case REJECT -> VerificationStatus.REJECTED;
            case REQUEST_MORE_INFO -> VerificationStatus.MORE_INFO_REQUESTED;
        };
        submission.setStatus(newStatus);
        String resolvedReviewer = StringUtils.hasText(reviewerId) ? reviewerId : "unknown-admin";
        auditLogs.save(new RecruiterReviewAuditLog(submission, resolvedReviewer, decision, reason));
        publishEvent(submission, newStatus.name());
        return toDetailDto(submission);
    }

    @Transactional(readOnly = true)
    public Page<RecruiterSubmissionDto> listHistory(String search, Pageable pageable) {
        String term = search == null ? "" : search.trim();
        if (!StringUtils.hasText(term)) {
            return submissions.findByStatusIn(HISTORY_STATUSES, pageable).map(this::toSummaryDto);
        }
        return submissions.findByStatusInAndCompanyNameContainingIgnoreCaseOrStatusInAndWorkEmailContainingIgnoreCase(
            HISTORY_STATUSES, term, HISTORY_STATUSES, term, pageable
        ).map(this::toSummaryDto);
    }

    private void publishEvent(RecruiterSubmission submission, String status) {
        try {
            RecruiterVerificationEvent event = new RecruiterVerificationEvent(
                String.valueOf(submission.getId()),
                submission.getRecruiterId(),
                submission.getWorkEmail(),
                submission.getCompanyName(),
                status
            );
            String payload = objectMapper.writeValueAsString(event);
            outboxEvents.save(new OutboxEvent("RECRUITER_VERIFICATION_STATUS", RabbitMQConfig.ROUTING_KEY, payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize RecruiterVerificationEvent", e);
        }
    }

    private RecruiterSubmissionDto toSummaryDto(RecruiterSubmission submission) {
        return new RecruiterSubmissionDto(
            submission.getId(),
            submission.getCompanyName(),
            submission.getWorkEmail(),
            submission.getEmailDomain(),
            submission.getCompanyWebsite(),
            submission.getRegistrationNumber(),
            submission.getContactName(),
            submission.getPhone(),
            submission.getDocumentUrl(),
            submission.getDocumentType(),
            submission.getStatus(),
            submission.getSubmittedAt(),
            List.of(),
            List.of()
        );
    }

    private RecruiterSubmissionDto toDetailDto(RecruiterSubmission submission) {
        List<AuditLogDto> logs = auditLogs.findBySubmissionIdOrderByReviewedAtDesc(submission.getId()).stream()
            .map(log -> new AuditLogDto(log.getId(), log.getReviewerId(), log.getDecision(), log.getReason(), log.getReviewedAt()))
            .toList();
        RecruiterSubmissionDto summary = toSummaryDto(submission);
        return new RecruiterSubmissionDto(
            summary.id(), summary.companyName(), summary.workEmail(), summary.emailDomain(), summary.companyWebsite(),
            summary.registrationNumber(), summary.contactName(), summary.phone(), summary.documentUrl(), summary.documentType(),
            summary.status(), summary.submittedAt(), buildRiskChecks(submission), logs
        );
    }

    private List<RiskCheckDto> buildRiskChecks(RecruiterSubmission submission) {
        String websiteDomain = normalizeDomain(submission.getCompanyWebsite());
        String emailDomain = normalizeDomain(submission.getEmailDomain());
        boolean domainMatch = StringUtils.hasText(websiteDomain) && emailDomain.equals(websiteDomain);
        boolean freeMail = emailDomain.matches("(gmail|yahoo|outlook|hotmail)\\.com");
        return List.of(
            new RiskCheckDto("domain_match", domainMatch, domainMatch ? "Email domain matches company website" : "Email domain differs from website", websiteDomain, emailDomain),
            new RiskCheckDto("business_email", !freeMail, freeMail ? "Work email uses a consumer mail domain" : "Work email uses a company-looking domain", "non-consumer domain", emailDomain),
            new RiskCheckDto("registration_number_present", StringUtils.hasText(submission.getRegistrationNumber()), "Registration number was supplied", "present", StringUtils.hasText(submission.getRegistrationNumber()) ? "present" : "missing")
        );
    }

    private String normalizeDomain(String value) {
        if (!StringUtils.hasText(value)) return "";
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        try {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                trimmed = "https://" + trimmed;
            }
            String host = URI.create(trimmed).getHost();
            return host == null ? "" : host.replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException ex) {
            return trimmed.replaceFirst("^www\\.", "");
        }
    }
}
