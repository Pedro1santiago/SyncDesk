package com.syncdesk.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final int maxAttempts;
    private final long windowSeconds;

    private final Map<String, AttemptWindow> attemptsByKey = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            @Value("${security.rate-limit.login.max-attempts:10}") int maxAttempts,
            @Value("${security.rate-limit.login.window-seconds:60}") long windowSeconds
    ) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveClientIp(request);
        RateLimitDecision decision = consumeAttempt(key);

        if (!decision.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many login attempts. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getServletPath());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private RateLimitDecision consumeAttempt(String key) {
        long now = Instant.now().getEpochSecond();

        synchronized (attemptsByKey) {
            AttemptWindow window = attemptsByKey.computeIfAbsent(key, ignored -> new AttemptWindow(now, 0));

            if (now - window.windowStart >= windowSeconds) {
                window.windowStart = now;
                window.attempts = 0;
            }

            if (window.attempts >= maxAttempts) {
                long retryAfter = Math.max(1, windowSeconds - (now - window.windowStart));
                return new RateLimitDecision(false, retryAfter);
            }

            window.attempts++;
            return new RateLimitDecision(true, 0);
        }
    }

    private static final class AttemptWindow {
        private long windowStart;
        private int attempts;

        private AttemptWindow(long windowStart, int attempts) {
            this.windowStart = windowStart;
            this.attempts = attempts;
        }
    }

    private record RateLimitDecision(boolean allowed, long retryAfterSeconds) {
    }
}
