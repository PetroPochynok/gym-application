package com.epam.gym.workload.filter;

import com.epam.gym.workload.util.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2)
public class WorkloadLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(WorkloadLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String txId = TransactionContext.get();

        LOG.info("[txId={}] Incoming request to Workload: {} {}",
                txId,
                request.getMethod(),
                request.getRequestURI()
        );

        try {
            filterChain.doFilter(request, response);

            LOG.info("[txId={}] Response status from Workload: {}",
                    txId,
                    response.getStatus()
            );
        } catch (Exception e) {
            LOG.error("[txId={}] Error in Workload: {}",
                    txId,
                    e.getMessage()
            );
            throw e;
        }
    }
}