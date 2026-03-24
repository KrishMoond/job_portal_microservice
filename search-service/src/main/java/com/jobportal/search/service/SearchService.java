package com.jobportal.search.service;

import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
