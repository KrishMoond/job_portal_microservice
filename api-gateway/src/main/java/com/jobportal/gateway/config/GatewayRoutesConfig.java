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
            // ── Unversioned routes (legacy, kept for backward compatibility) ──
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
            .route("recruiter-verification-submissions", r -> r.path("/api/recruiter-verifications/**")
                .uri("lb://recruiter-verification-service"))
            .route("recruiter-verification-service", r -> r.path("/api/admin/recruiter-verifications/**")
                .uri("lb://recruiter-verification-service"))
            // ── Versioned routes /api/v1/** (strip version prefix before forwarding) ──
            .route("v1-user-service",         r -> r.path("/api/v1/users/**", "/api/v1/bookmarks/**", "/api/v1/companies/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://user-service"))
            .route("v1-job-service",          r -> r.path("/api/v1/jobs/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://job-service"))
            .route("v1-app-service",          r -> r.path("/api/v1/applications/**", "/api/v1/interviews/**", "/api/v1/messages/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://application-service"))
            .route("v1-resume-service",       r -> r.path("/api/v1/resumes/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://resume-service"))
            .route("v1-search-service",       r -> r.path("/api/v1/search/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://search-service"))
            .route("v1-notification-service", r -> r.path("/api/v1/notifications/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://notification-service"))
            .route("v1-analytics-service",    r -> r.path("/api/v1/analytics/**", "/api/v1/recommendations/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://analytics-service"))
            .route("v1-recruiter-verification-submissions", r -> r.path("/api/v1/recruiter-verifications/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://recruiter-verification-service"))
            .route("v1-recruiter-verification-admin", r -> r.path("/api/v1/admin/recruiter-verifications/**")
                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://recruiter-verification-service"))
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
            .route("docs-recruiter-verification", r -> r.path("/v3/api-docs/recruiter-verification-service")
                .filters(f -> f.rewritePath("/v3/api-docs/recruiter-verification-service", "/v3/api-docs"))
                .uri("lb://recruiter-verification-service"))
            .build();
    }
}
