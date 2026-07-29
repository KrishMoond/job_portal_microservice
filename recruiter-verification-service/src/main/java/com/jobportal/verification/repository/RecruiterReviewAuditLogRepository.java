package com.jobportal.verification.repository;

import com.jobportal.verification.model.RecruiterReviewAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruiterReviewAuditLogRepository extends JpaRepository<RecruiterReviewAuditLog, Long> {
    List<RecruiterReviewAuditLog> findBySubmissionIdOrderByReviewedAtDesc(Long submissionId);
}
