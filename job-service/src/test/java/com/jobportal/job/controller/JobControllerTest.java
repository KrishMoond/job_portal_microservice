package com.jobportal.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.job.dto.JobRequest;
import com.jobportal.job.dto.JobResponse;
import com.jobportal.job.model.Job;
import com.jobportal.job.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JobService jobService;
    @Autowired ObjectMapper objectMapper;

    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        jobResponse = new JobResponse();
        jobResponse.setJobId("job-1");
        jobResponse.setTitle("Backend Developer");
        jobResponse.setCompany("TechCorp");
        jobResponse.setLocation("Remote");
        jobResponse.setStatus(Job.Status.OPEN);
    }

    @Test
    void create_recruiter_shouldReturnCreated() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Backend Developer");
        req.setCompanyId("comp-1");
        req.setLocation("Remote");
        req.setSalary("$90,000/yr");
        req.setDescription("Java role");

        when(jobService.createJob(any(), eq("user-1"), eq("RECRUITER"))).thenReturn(jobResponse);

        mockMvc.perform(post("/api/jobs")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value("job-1"));
    }

    @Test
    void create_jobSeeker_shouldReturnForbidden() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Backend Developer");
        req.setCompanyId("comp-1");
        req.setLocation("Remote");
        req.setSalary("$90,000/yr");
        req.setDescription("Java role");

        mockMvc.perform(post("/api/jobs")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(jobService.getById("job-1")).thenReturn(jobResponse);
        mockMvc.perform(get("/api/jobs/job-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value("job-1"));
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(jobResponse));
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jobId").value("job-1"));
    }

    @Test
    void closeJob_recruiter_shouldReturnOk() throws Exception {
        when(jobService.closeJob(eq("job-1"), eq("user-1"), eq("RECRUITER"))).thenReturn(jobResponse);
        mockMvc.perform(put("/api/jobs/job-1/close")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void closeJob_jobSeeker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/jobs/job-1/close")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reopenJob_recruiter_shouldReturnOk() throws Exception {
        when(jobService.reopenJob(eq("job-1"), eq("user-1"), eq("RECRUITER"))).thenReturn(jobResponse);
        mockMvc.perform(put("/api/jobs/job-1/reopen")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void reopenJob_jobSeeker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/jobs/job-1/reopen")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }
}
