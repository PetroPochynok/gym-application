package com.epam.gym.crm.filter;

import com.epam.gym.crm.context.TransactionContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RequestResponseLoggingFilterTest {

    private final RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter();

    @Test
    void shouldLogAndProceedSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        FilterChain filterChain = mock(FilterChain.class);

        TransactionContext.set("test-uuid-1234");

        try {
            filter.doFilter(request, response, filterChain);
            verify(filterChain, times(1)).doFilter(request, response);
        } finally {
            TransactionContext.clear();
        }
    }

    @Test
    void shouldLogErrorAndThrowExceptionWhenChainFails() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        doThrow(new RuntimeException("Database timeout")).when(filterChain).doFilter(any(), any());

        TransactionContext.set("error-uuid-5678");

        try {
            assertThrows(RuntimeException.class, () -> {
                filter.doFilter(request, response, filterChain);
            });
        } finally {
            TransactionContext.clear();
        }
    }
}