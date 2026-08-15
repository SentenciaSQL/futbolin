package com.futbolin.core.security;

import com.futbolin.core.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.core.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + bucket(request.getRequestURI());
        Window window = windows.computeIfAbsent(key, k -> new Window());
        if (!window.allow()) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    new ErrorResponse(Instant.now(), ErrorCode.RATE_LIMITED.name(), "Too many requests", List.of(), request.getRequestURI()));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String bucket(String uri) {
        if (uri.startsWith("/api/v1/auth")) {
            return "auth";
        }
        if (uri.startsWith("/api/v1/matches") || uri.startsWith("/ws")) {
            return "match";
        }
        return "api";
    }

    private static final class Window {
        private final AtomicInteger count = new AtomicInteger();
        private volatile long start = System.currentTimeMillis();

        synchronized boolean allow() {
            long now = System.currentTimeMillis();
            if (now - start > 60_000) {
                start = now;
                count.set(0);
            }
            return count.incrementAndGet() <= 120;
        }
    }
}
