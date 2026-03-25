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
            // API routes
            .route("user-service",         r -> r.path("/api/users/**", "/api/bookmarks/**", "/api/companies/**")
                .uri("lb://user-service"))
            .route("job-service",          r -> r.path("/api/jobs/**")
                .uri("lb://job-service"))
            .route("app-service",          r -> r.path("/api/applications/**", "/api/interviews/**", "/api/messages/**")
                .uri("lb://application-service"))
            .route("resume-service",       r -> r.path("/api/resumes/**")
                .uri("lb://resume-service"))
            .route("search-service",       r -> r.path("/api/search/**")
                .uri("lb://search-service"))
            .route("notification-service", r -> r.path("/api/notifications/**")
                .uri("lb://notification-service"))
            .route("analytics-service",    r -> r.path("/api/analytics/**", "/api/recommendations/**")
                .uri("lb://analytics-service"))
            // Swagger api-docs proxy routes
            .route("docs-user",         r -> r.path("/v3/api-docs/user-service")
                .filters(f -> f.rewritePath("/v3/api-docs/user-service", "/v3/api-docs"))
                .uri("lb://user-service"))
            .route("docs-job",          r -> r.path("/v3/api-docs/job-service")
                .filters(f -> f.rewritePath("/v3/api-docs/job-service", "/v3/api-docs"))
                .uri("lb://job-service"))
            .route("docs-app",          r -> r.path("/v3/api-docs/application-service")
                .filters(f -> f.rewritePath("/v3/api-docs/application-service", "/v3/api-docs"))
                .uri("lb://application-service"))
            .route("docs-resume",       r -> r.path("/v3/api-docs/resume-service")
                .filters(f -> f.rewritePath("/v3/api-docs/resume-service", "/v3/api-docs"))
                .uri("lb://resume-service"))
            .route("docs-search",       r -> r.path("/v3/api-docs/search-service")
                .filters(f -> f.rewritePath("/v3/api-docs/search-service", "/v3/api-docs"))
                .uri("lb://search-service"))
            .route("docs-notification", r -> r.path("/v3/api-docs/notification-service")
                .filters(f -> f.rewritePath("/v3/api-docs/notification-service", "/v3/api-docs"))
                .uri("lb://notification-service"))
            .route("docs-analytics",    r -> r.path("/v3/api-docs/analytics-service")
                .filters(f -> f.rewritePath("/v3/api-docs/analytics-service", "/v3/api-docs"))
                .uri("lb://analytics-service"))
            .build();
    }
}
