package com.relax.owl.common.log.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }

        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_ID, traceId);
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            try {
                log.info("[human] request method={} uri={} status={} cost={}ms traceId={}",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), cost, traceId);
            } catch (RuntimeException ignored) {
                // Request outcome must not depend on the logging backend.
            } finally {
                MDC.remove(TRACE_ID);
            }
        }
    }
}
