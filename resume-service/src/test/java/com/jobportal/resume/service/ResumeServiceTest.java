package com.jobportal.resume.service;

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
}
