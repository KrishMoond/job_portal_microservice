package com.jobportal.resume.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.messaging.ResumeEventPublisher;
import com.jobportal.resume.model.Resume;
import com.jobportal.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock ResumeRepository resumeRepository;
    @Mock StorageService storageService;
    @Mock ResumeEventPublisher eventPublisher;
    @InjectMocks ResumeService resumeService;

    private ResumeRequest req;
    private Resume savedResume;

    @BeforeEach
    void setUp() {
        req = new ResumeRequest();
        req.setUserId("user-1");
        req.setFileUrl("https://s3.amazonaws.com/bucket/resume.pdf");
        req.setFileName("resume.pdf");

        savedResume = new Resume();
        savedResume.setId("resume-1");
        savedResume.setUserId("user-1");
        savedResume.setFileUrl("https://s3.amazonaws.com/bucket/resume.pdf");
        savedResume.setFileName("resume.pdf");
        savedResume.setStorageType("URL");
    }

    @Test
    void upload_success() {
        when(storageService.store(req.getFileUrl(), req.getFileName()))
            .thenReturn("https://s3.amazonaws.com/bucket/resume.pdf");
        when(resumeRepository.save(any(Resume.class))).thenReturn(savedResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        ResumeResponse response = resumeService.upload(req);

        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getFileUrl()).isEqualTo("https://s3.amazonaws.com/bucket/resume.pdf");
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void getById_found() {
        when(resumeRepository.findById("resume-1")).thenReturn(Optional.of(savedResume));

        ResumeResponse response = resumeService.getById("resume-1");

        assertThat(response.getResumeId()).isEqualTo("resume-1");
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(resumeRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.getById("bad-id"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByUserId_returnsList() {
        when(resumeRepository.findByUserId("user-1")).thenReturn(List.of(savedResume));

        List<ResumeResponse> result = resumeService.getByUserId("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user-1");
    }

    @Test
    void getByUserId_noResumes_returnsEmpty() {
        when(resumeRepository.findByUserId("user-1")).thenReturn(List.of());

        assertThat(resumeService.getByUserId("user-1")).isEmpty();
    }

    // --- uploadFile tests ---

    @Test
    void uploadFile_nullFile_throwsBadRequest() {
        assertThatThrownBy(() -> resumeService.uploadFile("user-1", null))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("required");
    }

    @Test
    void uploadFile_emptyFile_throwsBadRequest() {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThatThrownBy(() -> resumeService.uploadFile("user-1", empty))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void uploadFile_invalidType_throwsBadRequest() {
        MockMultipartFile txt = new MockMultipartFile("file", "resume.txt", "text/plain", "data".getBytes());
        assertThatThrownBy(() -> resumeService.uploadFile("user-1", txt))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("PDF or image");
    }

    @Test
    void uploadFile_pdfByContentType_success() {
        MockMultipartFile pdf = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf-content".getBytes());
        Resume dbResume = buildDbResume("resume-2", "user-1", "resume.pdf", "application/pdf");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        ResumeResponse res = resumeService.uploadFile("user-1", pdf);

        assertThat(res.getStorageType()).isEqualTo("DB");
        assertThat(res.getFileUrl()).startsWith("/api/resumes/download/");
    }

    @Test
    void uploadFile_pdfByExtension_octetStream_normalizesContentType() {
        MockMultipartFile pdf = new MockMultipartFile("file", "resume.pdf", "application/octet-stream", "pdf-content".getBytes());
        Resume dbResume = buildDbResume("resume-3", "user-1", "resume.pdf", "application/pdf");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        ResumeResponse res = resumeService.uploadFile("user-1", pdf);

        assertThat(res.getStorageType()).isEqualTo("DB");
    }

    @Test
    void uploadFile_pngByExtension_octetStream_normalizesContentType() {
        MockMultipartFile png = new MockMultipartFile("file", "photo.png", "application/octet-stream", "img".getBytes());
        Resume dbResume = buildDbResume("resume-4", "user-1", "photo.png", "image/png");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        resumeService.uploadFile("user-1", png);

        verify(resumeRepository).save(any());
    }

    @Test
    void uploadFile_jpgByExtension_octetStream_normalizesContentType() {
        MockMultipartFile jpg = new MockMultipartFile("file", "photo.jpg", "application/octet-stream", "img".getBytes());
        Resume dbResume = buildDbResume("resume-5", "user-1", "photo.jpg", "image/jpeg");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        resumeService.uploadFile("user-1", jpg);

        verify(resumeRepository).save(any());
    }

    @Test
    void uploadFile_webpByExtension_octetStream_normalizesContentType() {
        MockMultipartFile webp = new MockMultipartFile("file", "photo.webp", "application/octet-stream", "img".getBytes());
        Resume dbResume = buildDbResume("resume-6", "user-1", "photo.webp", "image/webp");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        resumeService.uploadFile("user-1", webp);

        verify(resumeRepository).save(any());
    }

    @Test
    void uploadFile_imageByContentType_success() {
        MockMultipartFile img = new MockMultipartFile("file", "photo.png", "image/png", "img-data".getBytes());
        Resume dbResume = buildDbResume("resume-7", "user-1", "photo.png", "image/png");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        ResumeResponse res = resumeService.uploadFile("user-1", img);

        assertThat(res.getUserId()).isEqualTo("user-1");
    }

    @Test
    void uploadFile_blankFileName_usesDefault() {
        MockMultipartFile pdf = new MockMultipartFile("file", "", "application/pdf", "pdf-content".getBytes());
        Resume dbResume = buildDbResume("resume-8", "user-1", "resume", "application/pdf");
        when(resumeRepository.save(any())).thenReturn(dbResume);
        doNothing().when(eventPublisher).publishResumeUploaded(any());

        resumeService.uploadFile("user-1", pdf);

        verify(resumeRepository).save(argThat(r -> "resume".equals(r.getFileName())));
    }

    @Test
    void toResponse_dbStorageType_returnsDownloadUrl() {
        Resume dbResume = buildDbResume("resume-9", "user-1", "resume.pdf", "application/pdf");
        when(resumeRepository.findById("resume-9")).thenReturn(Optional.of(dbResume));

        ResumeResponse res = resumeService.getById("resume-9");

        assertThat(res.getFileUrl()).isEqualTo("/api/resumes/download/resume-9");
    }

    private Resume buildDbResume(String id, String userId, String fileName, String contentType) {
        Resume r = new Resume();
        r.setId(id);
        r.setUserId(userId);
        r.setFileName(fileName);
        r.setContentType(contentType);
        r.setStorageType("DB");
        r.setFileSize(100L);
        return r;
    }
}
