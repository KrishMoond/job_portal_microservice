package com.jobportal.search.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobSearchRecord>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(ApiResponse.success(
            searchService.search(keyword, location), "Search results"));
    }
}
