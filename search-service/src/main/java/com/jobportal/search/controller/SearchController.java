package com.jobportal.search.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /** Search jobs by keyword and/or location. */
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobSearchRecord>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(ApiResponse.success(
            searchService.search(keyword, location), "Search results"));
    }

    /** Search jobs within a specific category. */
    @GetMapping("/jobs/category/{category}")
    public ResponseEntity<ApiResponse<List<JobSearchRecord>>> searchByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
            searchService.searchByCategory(category), "Category results"));
    }

    /**
     * Returns live category counts from the DB.
     * Response: [ { "category": "Engineering", "count": 14 }, ... ]
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategoryCounts() {
        return ResponseEntity.ok(ApiResponse.success(
            searchService.getCategoryCounts(), "Category counts"));
    }
}
