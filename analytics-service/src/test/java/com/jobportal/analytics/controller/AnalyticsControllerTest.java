package com.jobportal.analytics.controller;

import com.jobportal.analytics.model.AnalyticsEvent;
import com.jobportal.analytics.repository.AnalyticsRepository;
import com.jobportal.analytics.service.MetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AnalyticsController.class, RecommendationController.class})
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MetricsService metricsService;
    @MockitoBean AnalyticsRepository analyticsRepository;

    @Test
    void getSummary_recruiter_shouldReturnOk() throws Exception {
        when(metricsService.getSummary()).thenReturn(Map.of("jobsPosted", 5L, "applications", 10L, "resumeUploads", 3L));
        mockMvc.perform(get("/api/analytics/summary").header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobsPosted").value(5));
    }

    @Test
    void getSummary_admin_shouldReturnOk() throws Exception {
        when(metricsService.getSummary()).thenReturn(Map.of("jobsPosted", 2L, "applications", 4L, "resumeUploads", 1L));
        mockMvc.perform(get("/api/analytics/summary").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getSummary_jobSeeker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/analytics/summary").header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRecommendations_returnsFilteredJobAppliedEvents() throws Exception {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setEventType("JOB_APPLIED");
        e.setJobId("job-1");
        e.setUserId("user-1");
        when(analyticsRepository.findByUserId("user-1")).thenReturn(List.of(e));

        mockMvc.perform(get("/api/recommendations").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value("job-1"));
    }

    @Test
    void getRecommendations_noEvents_returnsEmpty() throws Exception {
        when(analyticsRepository.findByUserId("user-1")).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/recommendations").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRecommendations_filtersOutNonJobApplied() throws Exception {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setEventType("JOB_CREATED");
        e.setJobId("job-1");
        e.setUserId("user-1");
        when(analyticsRepository.findByUserId("user-1")).thenReturn(List.of(e));

        mockMvc.perform(get("/api/recommendations").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
