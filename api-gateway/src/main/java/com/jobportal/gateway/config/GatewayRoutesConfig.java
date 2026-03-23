package com.jobportal.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service",   r -> r.path("/api/users/**")
                .uri("lb://user-service"))
            .route("job-service",    r -> r.path("/api/jobs/**")
                .uri("lb://job-service"))
            .route("app-service",    r -> r.path("/api/applications/**")
                .uri("lb://application-service"))
            .route("resume-service", r -> r.path("/api/resumes/**")
                .uri("lb://resume-service"))
            .route("search-service", r -> r.path("/api/search/**")
                .uri("lb://search-service"))
            .route("analytics-service", r -> r.path("/api/analytics/**")
                .uri("lb://analytics-service"))
            .build();
    }
}
