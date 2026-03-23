package com.jobportal.search.repository;

import com.jobportal.search.model.JobSearchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobSearchRepository extends JpaRepository<JobSearchRecord, String> {
    Optional<JobSearchRecord> findByJobId(String jobId);

    @Query("SELECT j FROM JobSearchRecord j WHERE j.status = 'OPEN' AND " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<JobSearchRecord> search(@Param("keyword") String keyword,
                                  @Param("location") String location);
}
