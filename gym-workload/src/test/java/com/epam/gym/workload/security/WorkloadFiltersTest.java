package com.epam.gym.workload.security;

import com.epam.gym.workload.filter.WorkloadLoggingFilter;
import com.epam.gym.workload.util.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadFiltersTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @InjectMocks
    private WorkloadLoggingFilter workloadLoggingFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TransactionContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TransactionContext.clear();
    }

    @Test
    void jwtAuthFilter_shouldSkip_whenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtAuthFilter_shouldSkip_whenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtAuthFilter_shouldAuthenticateWithDefaultRole_whenRolesEmpty() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtProvider.getUsernameFromToken("valid-token")).thenReturn("olga.k");
        when(jwtProvider.getRolesFromToken("valid-token")).thenReturn(Collections.emptyList());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("olga.k", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals("ROLE_USER", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void jwtAuthFilter_shouldHandleException_whenTokenInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(filterChain);
        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertTrue(response.getContentAsString().contains("Invalid JWT token"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void workloadLoggingFilter_shouldLogAndProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workload");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TransactionContext.set("tx-123");

        workloadLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void workloadLoggingFilter_shouldLogAndThrow_whenChainThrowsException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workload");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TransactionContext.set("tx-123");

        doThrow(new RuntimeException("Chain error")).when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> workloadLoggingFilter.doFilter(request, response, filterChain));
    }
}