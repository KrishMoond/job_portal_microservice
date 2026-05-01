package com.jobportal.search.service;

import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final JobSearchRepository jobSearchRepository;

    public SearchService(JobSearchRepository jobSearchRepository) {
        this.jobSearchRepository = jobSearchRepository;
    }

    public List<JobSearchRecord> search(String keyword, String location) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasLocation = location != null && !location.isBlank();

        if (hasKeyword && hasLocation) return jobSearchRepository.findByKeywordAndLocation(keyword, location);
        if (hasKeyword)                return jobSearchRepository.findByKeyword(keyword);
        if (hasLocation)               return jobSearchRepository.findByLocationContaining(location);
        return jobSearchRepository.findByStatus("OPEN");
    }

    /** Returns jobs in a specific category (OPEN only). */
    public List<JobSearchRecord> searchByCategory(String category) {
        return jobSearchRepository.findByStatusAndCategory("OPEN", category);
    }

    /**
     * Returns live category counts from the DB.
     * Each entry: { "category": "Engineering", "count": 14 }
     */
    public List<Map<String, Object>> getCategoryCounts() {
        return jobSearchRepository.countByCategory();
    }
}
