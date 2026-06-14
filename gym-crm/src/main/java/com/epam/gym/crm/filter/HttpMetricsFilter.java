package com.epam.gym.crm.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpMetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    public HttpMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            filterChain.doFilter(request, response);
        } finally {
            String sanitizedUri = getSanitizedUri(request);
            String method = request.getMethod();
            String status = getStatusString(response);

            meterRegistry.counter("gymcrm_http_requests_total",
                            "method", method,
                            "uri", sanitizedUri,
                            "status", status)
                    .increment();

            sample.stop(Timer.builder("gymcrm_http_request_duration_seconds")
                    .description("HTTP request duration in seconds")
                    .tag("method", method)
                    .tag("uri", sanitizedUri)
                    .tag("status", status)
                    .register(meterRegistry));
        }
    }

    private String getSanitizedUri(HttpServletRequest request) {
        Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchingPattern != null) {
            return bestMatchingPattern.toString();
        }

        String uri = request.getRequestURI();
        if (uri == null || uri.isEmpty()) {
            return "root";
        }

        return uri.replaceAll("/\\d+", "/{id}");
    }

    private String getStatusString(HttpServletResponse response) {
        int status = response.getStatus();
        return status <= 0 ? "500" : String.valueOf(status);
    }
}