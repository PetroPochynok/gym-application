package com.epam.gym.crm.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HttpMetricsFilterTest {

    private MeterRegistry meterRegistry;
    private HttpMetricsFilter filter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        filter = new HttpMetricsFilter(meterRegistry);
    }

    @Test
    void shouldRecordCounterAndTimerWithSanitizedUri() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/trainers/45");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(204);

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        double requestCount = meterRegistry.counter("gymcrm_http_requests_total",
                "method", "DELETE",
                "uri", "/api/trainers/{id}",
                "status", "204").count();

        assertEquals(1.0, requestCount);

        var timer = meterRegistry.find("gymcrm_http_request_duration_seconds")
                .tag("method", "DELETE")
                .tag("uri", "/api/trainers/{id}")
                .tag("status", "204")
                .timer();

        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.SECONDS) >= 0);
    }

    @Test
    void shouldPreferBestMatchingPatternAttributeIfPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/5/trainers");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/trainees/{id}/trainers");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        filter.doFilter(request, response, mock(FilterChain.class));

        double requestCount = meterRegistry.counter("gymcrm_http_requests_total",
                "method", "GET",
                "uri", "/api/trainees/{id}/trainers",
                "status", "200").count();

        assertEquals(1.0, requestCount);
    }
}