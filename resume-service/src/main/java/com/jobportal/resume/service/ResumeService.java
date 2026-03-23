package com.jobportal.resume.service;

import com.jobportal.common.events.ResumeUploadedEvent;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.resume.dto.ResumeRequest;
import com.jobportal.resume.dto.ResumeResponse;
import com.jobportal.resume.messaging.ResumeEventPublisher;
import com.jobportal.resume.model.Resume;
import com.jobportal.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;

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

    public ResumeResponse upload(ResumeRequest req) {
        String storedUrl = storageService.store(req.getFileUrl(), req.getFileName());
        Resume resume = new Resume();
        resume.setUserId(req.getUserId());
        resume.setFileUrl(storedUrl);
        resume.setFileName(req.getFileName());
        resume = resumeRepository.save(resume);

        ResumeUploadedEvent event = new ResumeUploadedEvent(
            resume.getId(), resume.getUserId(), storedUrl);
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
        res.setFileUrl(r.getFileUrl());
        res.setFileName(r.getFileName());
        res.setUploadedAt(r.getUploadedAt());
        return res;
    }
}
