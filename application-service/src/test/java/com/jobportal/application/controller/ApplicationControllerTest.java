package com.jobportal.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.application.dto.ApplyRequest;
import com.jobportal.application.dto.InterviewRequest;
import com.jobportal.application.dto.StatusUpdateRequest;
import com.jobportal.application.model.Interview;
import com.jobportal.application.model.JobApplication;
import com.jobportal.application.service.ApplicationService;
import com.jobportal.application.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ApplicationController.class, InterviewController.class})
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ApplicationService applicationService;
    @MockitoBean InterviewService interviewService;
    @Autowired ObjectMapper objectMapper;

    private JobApplication savedApp;
    private Interview savedInterview;

    @BeforeEach
    void setUp() {
        savedApp = new JobApplication();
        savedApp.setId("app-1");
        savedApp.setJobId("job-1");
        savedApp.setCandidateId("cand-1");
        savedApp.setStatus(JobApplication.Status.APPLIED);

        savedInterview = new Interview();
        savedInterview.setId("iv-1");
        savedInterview.setApplicationId("app-1");
        savedInterview.setCandidateId("cand-1");
        savedInterview.setRecruiterId("rec-1");
        savedInterview.setScheduledAt(LocalDateTime.now().plusDays(1));
        savedInterview.setStatus("SCHEDULED");
    }

    // ── ApplicationController ─────────────────────────────────────────────────

    @Test
    void apply_jobSeeker_shouldReturnCreated() throws Exception {
        ApplyRequest req = new ApplyRequest();
        req.setJobId("job-1");
        req.setCandidateId("cand-1");
        req.setResumeId("res-1");

        when(applicationService.apply(any())).thenReturn(savedApp);

        mockMvc.perform(post("/api/applications")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("app-1"));
    }

    @Test
    void apply_recruiter_shouldReturnForbidden() throws Exception {
        ApplyRequest req = new ApplyRequest();
        req.setJobId("job-1");
        req.setCandidateId("cand-1");
        req.setResumeId("res-1");

        mockMvc.perform(post("/api/applications")
                .header("X-User-Id", "rec-1")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByJob_recruiter_shouldReturnOk() throws Exception {
        when(applicationService.getByJobId(eq("job-1"), eq("rec-1"), eq("RECRUITER")))
                .thenReturn(List.of(savedApp));

        mockMvc.perform(get("/api/applications/job/job-1")
                .header("X-User-Id", "rec-1")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("app-1"));
    }

    @Test
    void getByJob_jobSeeker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/applications/job/job-1")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByCandidate_shouldReturnOk() throws Exception {
        when(applicationService.getByCandidateId(eq("cand-1"), eq("cand-1"), eq("JOB_SEEKER")))
                .thenReturn(List.of(savedApp));

        mockMvc.perform(get("/api/applications/candidate/cand-1")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("app-1"));
    }

    @Test
    void updateStatus_recruiter_shouldReturnOk() throws Exception {
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        when(applicationService.updateStatus(eq("app-1"), any(), eq("rec-1"), eq("RECRUITER")))
                .thenReturn(savedApp);

        mockMvc.perform(put("/api/applications/app-1/status")
                .header("X-User-Id", "rec-1")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatus_jobSeeker_shouldReturnForbidden() throws Exception {
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(JobApplication.Status.SHORTLISTED);

        mockMvc.perform(put("/api/applications/app-1/status")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void respondToOffer_jobSeeker_shouldReturnOk() throws Exception {
        when(applicationService.respondToOffer(eq("app-1"), eq(true), eq("cand-1"), eq("JOB_SEEKER")))
                .thenReturn(savedApp);

        mockMvc.perform(post("/api/applications/app-1/offer-response")
                .param("accepted", "true")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Offer accepted"));
    }

    @Test
    void respondToOffer_recruiter_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/applications/app-1/offer-response")
                .param("accepted", "true")
                .header("X-User-Id", "rec-1")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    // ── InterviewController ───────────────────────────────────────────────────

    @Test
    void scheduleInterview_shouldReturnCreated() throws Exception {
        InterviewRequest req = new InterviewRequest();
        req.setApplicationId("app-1");
        req.setCandidateId("cand-1");
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setMeetingLink("https://zoom.us/j/123");

        when(interviewService.schedule(any(), eq("rec-1"))).thenReturn(savedInterview);

        mockMvc.perform(post("/api/interviews")
                .header("X-User-Id", "rec-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("iv-1"));
    }

    @Test
    void updateInterviewStatus_shouldReturnOk() throws Exception {
        when(interviewService.updateStatus("iv-1", "COMPLETED", "rec-1")).thenReturn(savedInterview);

        mockMvc.perform(put("/api/interviews/iv-1/status")
                .param("status", "COMPLETED")
                .header("X-User-Id", "rec-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getByApplication_shouldReturnOk() throws Exception {
        when(interviewService.getByApplicationId("app-1")).thenReturn(List.of(savedInterview));

        mockMvc.perform(get("/api/interviews/application/app-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("iv-1"));
    }

    @Test
    void getMyInterviews_shouldReturnOk() throws Exception {
        when(interviewService.getMyInterviews("cand-1", "JOB_SEEKER")).thenReturn(List.of(savedInterview));

        mockMvc.perform(get("/api/interviews/mine")
                .header("X-User-Id", "cand-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("iv-1"));
    }
}
