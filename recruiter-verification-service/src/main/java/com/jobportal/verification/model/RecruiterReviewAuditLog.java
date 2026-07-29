package com.jobportal.verification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruiter_review_audit_logs")
public class RecruiterReviewAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private RecruiterSubmission submission;

    @Column(nullable = false)
    private String reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewDecision decision;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime reviewedAt = LocalDateTime.now();

    protected RecruiterReviewAuditLog() {}

    public RecruiterReviewAuditLog(RecruiterSubmission submission, String reviewerId, ReviewDecision decision, String reason) {
        this.submission = submission;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public RecruiterSubmission getSubmission() { return submission; }
    public String getReviewerId() { return reviewerId; }
    public ReviewDecision getDecision() { return decision; }
    public String getReason() { return reason; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
