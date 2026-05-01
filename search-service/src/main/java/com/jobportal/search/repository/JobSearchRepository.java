package com.jobportal.search.repository;

import com.jobportal.search.model.JobSearchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JobSearchRepository extends JpaRepository<JobSearchRecord, String> {
    Optional<JobSearchRecord> findByJobId(String jobId);

    List<JobSearchRecord> findByStatus(String status);

    @Query("SELECT j FROM JobSearchRecord j WHERE j.status = 'OPEN' " +
           "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<JobSearchRecord> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT j FROM JobSearchRecord j WHERE j.status = 'OPEN' " +
           "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<JobSearchRecord> findByLocationContaining(@Param("location") String location);

    @Query("SELECT j FROM JobSearchRecord j WHERE j.status = 'OPEN' " +
           "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<JobSearchRecord> findByKeywordAndLocation(@Param("keyword") String keyword, @Param("location") String location);

    // Category-based queries
    List<JobSearchRecord> findByStatusAndCategory(String status, String category);

    @Query("SELECT j.category AS category, COUNT(j) AS count " +
           "FROM JobSearchRecord j " +
           "WHERE j.status = 'OPEN' AND j.category IS NOT NULL AND j.category <> '' " +
           "GROUP BY j.category " +
           "ORDER BY COUNT(j) DESC")
    List<Map<String, Object>> countByCategory();
}
