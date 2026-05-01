package com.jobportal.application.repository;

import com.jobportal.application.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<JobApplication, String> {
    List<JobApplication> findByJobId(String jobId);
    List<JobApplication> findByCandidateId(String candidateId);
    boolean existsByJobIdAndCandidateId(String jobId, String candidateId);

    @Query("select max(a.recruiterId) from JobApplication a where a.jobId = :jobId")
    String findRecruiterIdByJobId(@Param("jobId") String jobId);
}
