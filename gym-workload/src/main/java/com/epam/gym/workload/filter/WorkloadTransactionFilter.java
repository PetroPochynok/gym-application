package com.epam.gym.workload.filter;

import com.epam.gym.workload.util.TransactionContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class WorkloadTransactionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String txId = request.getHeader("X-Transaction-Id");

        if (txId == null || txId.isBlank()) {
            txId = UUID.randomUUID().toString();
        }

        TransactionContext.set(txId);
        response.setHeader("X-Transaction-Id", txId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TransactionContext.clear();
        }
    }
}