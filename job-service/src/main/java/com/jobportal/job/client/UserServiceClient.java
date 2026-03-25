package com.jobportal.job.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{userId}")
    Map<String, Object> getUserById(@PathVariable String userId);

    @GetMapping("/api/companies/{companyId}")
    Map<String, Object> getCompanyById(@PathVariable String companyId);
}
