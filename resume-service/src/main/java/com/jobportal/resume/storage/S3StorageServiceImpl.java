package com.jobportal.resume.storage;

import com.jobportal.resume.service.StorageService;
import org.springframework.stereotype.Service;

@Service
public class S3StorageServiceImpl implements StorageService {
    @Override
    public String store(String fileUrl, String fileName) {
        // In production: upload to S3 using AWS SDK and return S3 URL
        // For now: return the provided fileUrl as-is
        return fileUrl;
    }
}
