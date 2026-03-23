package com.jobportal.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "job-service")
public interface JobServiceClient {
    @GetMapping("/api/jobs/{jobId}")
    Map<String, Object> getJobById(@PathVariable String jobId);
}
