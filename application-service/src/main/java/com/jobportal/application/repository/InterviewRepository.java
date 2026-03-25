package com.jobportal.application.repository;

import com.jobportal.application.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, String> {
    List<Interview> findByApplicationId(String applicationId);
    List<Interview> findByCandidateIdOrderByScheduledAtAsc(String candidateId);
    List<Interview> findByRecruiterIdOrderByScheduledAtAsc(String recruiterId);
}