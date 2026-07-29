package com.jobportal.verification.repository;

import com.jobportal.verification.model.RecruiterSubmission;
import com.jobportal.verification.model.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RecruiterSubmissionRepository extends JpaRepository<RecruiterSubmission, Long> {
    Page<RecruiterSubmission> findByStatusIn(Collection<VerificationStatus> statuses, Pageable pageable);
    Page<RecruiterSubmission> findByStatus(VerificationStatus status, Pageable pageable);
    Page<RecruiterSubmission> findByStatusInAndCompanyNameContainingIgnoreCaseOrStatusInAndWorkEmailContainingIgnoreCase(
        Collection<VerificationStatus> companyStatuses,
        String companyName,
        Collection<VerificationStatus> emailStatuses,
        String workEmail,
        Pageable pageable
    );
}
