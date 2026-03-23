package com.jobportal.job.repository;

import com.jobportal.job.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByStatus(Job.Status status);
    List<Job> findByRecruiterId(String recruiterId);
}
