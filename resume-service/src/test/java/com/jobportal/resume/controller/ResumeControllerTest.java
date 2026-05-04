package com.jobportal.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.exception.GlobalExceptionHandler;
import com.jobportal.resume.model.Resume;
import com.jobportal.resume.repository.ResumeRepository;
import com.jobportal.resume.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ResumeService resumeService;
    @MockBean ResumeRepository resumeRepository;

    private ResumeResponse resumeResponse;

    @BeforeEach
    void setUp() {
        resumeResponse = new ResumeResponse();
        resumeResponse.setResumeId("resume-1");
        resumeResponse.setUserId("user-1");
        resumeResponse.setFileUrl("https://s3.amazonaws.com/bucket/resume.pdf");
        resumeResponse.setFileName("resume.pdf");
        resumeResponse.setStorageType("URL");
    }

    // --- POST /api/resumes ---

    @Test
    void upload_jobSeeker_success() throws Exception {
        when(resumeService.upload(any())).thenReturn(resumeResponse);

        mockMvc.perform(post("/api/resumes")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"user-1\",\"fileUrl\":\"https://example.com/resume.pdf\",\"fileName\":\"resume.pdf\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.resumeId").value("resume-1"));
    }

    @Test
    void upload_recruiter_forbidden() throws Exception {
        mockMvc.perform(post("/api/resumes")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"user-1\",\"fileUrl\":\"https://example.com/resume.pdf\",\"fileName\":\"resume.pdf\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void upload_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/resumes")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"fileUrl\":\"not-a-url\"}"))
            .andExpect(status().isBadRequest());
    }

    // --- POST /api/resumes/upload ---

    @Test
    void uploadFile_jobSeeker_success() throws Exception {
        when(resumeService.uploadFile(eq("user-1"), any())).thenReturn(resumeResponse);

        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());
        mockMvc.perform(multipart("/api/resumes/upload")
                .file(file)
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.resumeId").value("resume-1"));
    }

    @Test
    void uploadFile_recruiter_forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());
        mockMvc.perform(multipart("/api/resumes/upload")
                .file(file)
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "RECRUITER"))
            .andExpect(status().isForbidden());
    }

    @Test
    void uploadFile_missingUserId_forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());
        mockMvc.perform(multipart("/api/resumes/upload")
                .file(file)
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isForbidden());
    }

    // --- GET /api/resumes/{resumeId} ---

    @Test
    void getById_owner_success() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/resume-1")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.resumeId").value("resume-1"));
    }

    @Test
    void getById_recruiter_canViewAnyResume() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/resume-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "RECRUITER"))
            .andExpect(status().isOk());
    }

    @Test
    void getById_admin_canViewAnyResume() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/resume-1")
                .header("X-User-Id", "admin-1")
                .header("X-User-Role", "ADMIN"))
            .andExpect(status().isOk());
    }

    @Test
    void getById_differentUser_forbidden() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/resume-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(resumeService.getById("bad-id")).thenThrow(new ResourceNotFoundException("Resume not found: bad-id"));

        mockMvc.perform(get("/api/resumes/bad-id")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isNotFound());
    }

    // --- GET /api/resumes/download/{resumeId} ---

    @Test
    void download_urlBacked_redirects() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/download/resume-1")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isFound());
    }

    @Test
    void download_dbBacked_returnsBytes() throws Exception {
        ResumeResponse dbResponse = new ResumeResponse();
        dbResponse.setResumeId("resume-2");
        dbResponse.setUserId("user-1");
        dbResponse.setStorageType("DB");
        dbResponse.setFileName("resume.pdf");
        dbResponse.setContentType("application/pdf");
        when(resumeService.getById("resume-2")).thenReturn(dbResponse);

        Resume entity = new Resume();
        entity.setId("resume-2");
        entity.setUserId("user-1");
        entity.setFileName("resume.pdf");
        entity.setContentType("application/pdf");
        entity.setFileData("pdf-bytes".getBytes());
        entity.setStorageType("DB");
        when(resumeRepository.findById("resume-2")).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/resumes/download/resume-2")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void download_dbBacked_nullContentType_usesOctetStream() throws Exception {
        ResumeResponse dbResponse = new ResumeResponse();
        dbResponse.setResumeId("resume-3");
        dbResponse.setUserId("user-1");
        dbResponse.setStorageType("DB");
        when(resumeService.getById("resume-3")).thenReturn(dbResponse);

        Resume entity = new Resume();
        entity.setId("resume-3");
        entity.setUserId("user-1");
        entity.setFileData("bytes".getBytes());
        entity.setStorageType("DB");
        when(resumeRepository.findById("resume-3")).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/resumes/download/resume-3")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/octet-stream"));
    }

    @Test
    void download_differentUser_forbidden() throws Exception {
        when(resumeService.getById("resume-1")).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/resumes/download/resume-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isForbidden());
    }

    // --- GET /api/resumes/user/{userId} ---

    @Test
    void getByUser_owner_success() throws Exception {
        when(resumeService.getByUserId("user-1")).thenReturn(List.of(resumeResponse));

        mockMvc.perform(get("/api/resumes/user/user-1")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].resumeId").value("resume-1"));
    }

    @Test
    void getByUser_admin_canViewAny() throws Exception {
        when(resumeService.getByUserId("user-1")).thenReturn(List.of(resumeResponse));

        mockMvc.perform(get("/api/resumes/user/user-1")
                .header("X-User-Id", "admin-1")
                .header("X-User-Role", "ADMIN"))
            .andExpect(status().isOk());
    }

    @Test
    void getByUser_differentUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/user/user-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "JOB_SEEKER"))
            .andExpect(status().isForbidden());
    }
}
