package com.fintrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.common.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
 * Spec'in "özellikle authentication endpointlerinde uygulanmalı" kuralı —
 * yalnızca {@code /auth/login} ve {@code /auth/register} için, IP bazlı.
 * Kimliği doğrulanmış diğer endpoint'lerde brute-force riski farklı bir
 * doğada olduğu için (zaten geçerli bir JWT gerektiriyorlar) burada
 * kapsanmadı.
 */
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password"
    );

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Value("${fintrack.rate-limit.auth-max-requests:5}")
    private int maxRequests;

    @Value("${fintrack.rate-limit.auth-window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (LIMITED_PATHS.contains(request.getRequestURI())) {
            String key = "rate-limit:%s:%s".formatted(request.getRequestURI(), clientIp(request));
            boolean allowed = rateLimiterService.tryConsume(key, maxRequests, Duration.ofSeconds(windowSeconds));
            if (!allowed) {
                writeTooManyRequests(response, request.getRequestURI());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(windowSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Çok fazla istek gönderdin, lütfen %d saniye sonra tekrar dene".formatted(windowSeconds),
                path
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
