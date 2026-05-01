package com.jobportal.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/users/register",
        "/api/users/login",
        "/api/search/jobs",
        "/api/search/categories",
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs",
        "/webjars"
    );

    // Map.of() supports max 10 entries — use Map.ofEntries() for 11+
    private static final Map<String, Map<HttpMethod, List<String>>> ROLE_RULES = Map.ofEntries(
        Map.entry("/api/jobs", Map.of(
            HttpMethod.POST, List.of("RECRUITER", "ADMIN"),
            HttpMethod.PUT,  List.of("RECRUITER", "ADMIN"),
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/applications", Map.of(
            HttpMethod.POST, List.of("JOB_SEEKER"),
            HttpMethod.PUT,  List.of("RECRUITER", "ADMIN"),
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/resumes", Map.of(
            HttpMethod.POST, List.of("JOB_SEEKER"),
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/analytics", Map.of(
            HttpMethod.GET,  List.of("ADMIN", "RECRUITER")
        )),
        Map.entry("/api/users", Map.of(
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN"),
            HttpMethod.PUT,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/bookmarks", Map.of(
            HttpMethod.GET,    List.of("JOB_SEEKER"),
            HttpMethod.POST,   List.of("JOB_SEEKER"),
            HttpMethod.DELETE, List.of("JOB_SEEKER")
        )),
        Map.entry("/api/companies", Map.of(
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN"),
            HttpMethod.POST, List.of("RECRUITER", "ADMIN"),
            HttpMethod.PUT,  List.of("RECRUITER", "ADMIN")
        )),
        Map.entry("/api/notifications", Map.of(
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN"),
            HttpMethod.PUT,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/interviews", Map.of(
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN"),
            HttpMethod.POST, List.of("RECRUITER", "ADMIN"),
            HttpMethod.PUT,  List.of("RECRUITER", "ADMIN")
        )),
        Map.entry("/api/messages", Map.of(
            HttpMethod.GET,  List.of("RECRUITER", "JOB_SEEKER", "ADMIN"),
            HttpMethod.POST, List.of("RECRUITER", "JOB_SEEKER", "ADMIN")
        )),
        Map.entry("/api/recommendations", Map.of(
            HttpMethod.GET,  List.of("JOB_SEEKER")
        ))
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims;
        try {
            String token = authHeader.substring(7);
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String role = claims.get("role", String.class);

        if (!isAuthorized(path, method, role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        String userId = claims.getSubject();
        ServerWebExchange mutated = exchange.mutate()
            .request(r -> r.headers(h -> {
                h.set("X-User-Id", userId);
                h.set("X-User-Role", role);
            }))
            .build();

        return chain.filter(mutated);
    }

    private boolean isAuthorized(String path, HttpMethod method, String role) {
        for (Map.Entry<String, Map<HttpMethod, List<String>>> entry : ROLE_RULES.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                List<String> allowedRoles = entry.getValue().get(method);
                return allowedRoles != null && allowedRoles.contains(role);
            }
        }
        return false;
    }

    @Override
    public int getOrder() { return -1; }
}
