package com.jobportal.application.repository;

import com.jobportal.application.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<JobApplication, String> {
    List<JobApplication> findByJobId(String jobId);
    List<JobApplication> findByCandidateId(String candidateId);
    boolean existsByJobIdAndCandidateId(String jobId, String candidateId);
}
