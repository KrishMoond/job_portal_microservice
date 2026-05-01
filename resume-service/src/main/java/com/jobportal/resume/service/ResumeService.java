package com.jobportal.resume.service;

import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.messaging.ResumeEventPublisher;
import com.jobportal.resume.model.Resume;
import com.jobportal.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final StorageService storageService;
    private final ResumeEventPublisher eventPublisher;

    public ResumeService(ResumeRepository resumeRepository,
                          StorageService storageService,
                          ResumeEventPublisher eventPublisher) {
        this.resumeRepository = resumeRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
    }

    @org.springframework.transaction.annotation.Transactional
    public ResumeResponse upload(ResumeRequest req) {
        String storedUrl = storageService.store(req.getFileUrl(), req.getFileName());
        Resume resume = new Resume();
        resume.setUserId(req.getUserId());
        resume.setFileUrl(storedUrl);
        resume.setFileName(req.getFileName());
        resume.setStorageType("URL");
        resume = resumeRepository.save(resume);

        ResumeUploadedEvent event = new ResumeUploadedEvent(
            resume.getId(), resume.getUserId(), storedUrl);
        eventPublisher.publishResumeUploaded(event);

        return toResponse(resume);
    }

    @org.springframework.transaction.annotation.Transactional
    public ResumeResponse uploadFile(String userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Resume file is required");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lowerFileName = originalName.toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean isPdfByExt = lowerFileName.endsWith(".pdf");
        boolean isAllowedImageByExt = lowerFileName.endsWith(".png")
                || lowerFileName.endsWith(".jpg")
                || lowerFileName.endsWith(".jpeg")
                || lowerFileName.endsWith(".webp");
        boolean allowed = contentType.equals("application/pdf")
                || contentType.equals("application/x-pdf")
                || contentType.startsWith("image/")
                || isPdfByExt
                || isAllowedImageByExt;
        if (!allowed) {
            throw new BadRequestException("Only PDF or image files are allowed");
        }
        if (contentType.isBlank() || "application/octet-stream".equals(contentType)) {
            if (isPdfByExt) {
                contentType = "application/pdf";
            } else if (lowerFileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFileName.endsWith(".webp")) {
                contentType = "image/webp";
            }
        }

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileName(originalName.isBlank() ? "resume" : originalName);
        resume.setContentType(contentType);
        resume.setFileSize(file.getSize());
        resume.setStorageType("DB");
        try {
            resume.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file");
        }
        resume = resumeRepository.save(resume);

        ResumeUploadedEvent event = new ResumeUploadedEvent(
                resume.getId(), resume.getUserId(), "/api/resumes/download/" + resume.getId());
        eventPublisher.publishResumeUploaded(event);
        return toResponse(resume);
    }

    public ResumeResponse getById(String resumeId) {
        return toResponse(resumeRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId)));
    }

    public List<ResumeResponse> getByUserId(String userId) {
        return resumeRepository.findByUserId(userId).stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    private ResumeResponse toResponse(Resume r) {
        ResumeResponse res = new ResumeResponse();
        res.setResumeId(r.getId());
        res.setUserId(r.getUserId());
        res.setFileUrl("DB".equalsIgnoreCase(r.getStorageType()) ? "/api/resumes/download/" + r.getId() : r.getFileUrl());
        res.setFileName(r.getFileName());
        res.setContentType(r.getContentType());
        res.setFileSize(r.getFileSize());
        res.setStorageType(r.getStorageType());
        res.setUploadedAt(r.getUploadedAt());
        return res;
    }
}
