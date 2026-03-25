package com.jobportal.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private GatewayFilterChain filterChain;

    private final String SECRET = "j0bP0rt@lS3cur3S3cr3tK3y!XyZ#2024$AbCdEfGhIjKlMnOpQrStUvWxYz12";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtSecret", SECRET);
    }

    private String generateToken(String role, String userId) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }

    @Test
    void shouldAllowPublicPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldRejectWhenNoAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/jobs").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectWhenInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowValidTokenWithCorrectRole() {
        String token = generateToken("RECRUITER", "123");
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldRejectValidTokenWithIncorrectRole() {
        String token = generateToken("JOB_SEEKER", "123"); // JOB_SEEKER is not allowed to POST to /api/jobs
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectUnmappedPath() {
        String token = generateToken("ADMIN", "123");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/unknown")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldFilterAndMutateHeadersOnSuccess() {
        String token = generateToken("JOB_SEEKER", "789");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange mutatedExchange = invocation.getArgument(0);
            assertEquals("789", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals("JOB_SEEKER", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role"));
            return Mono.empty();
        });

        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);
        StepVerifier.create(result).verifyComplete();
    }
}
