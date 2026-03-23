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
        return jobSearchRepository.search(keyword, location);
    }
}
