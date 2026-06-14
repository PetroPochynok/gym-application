package com.epam.gym.crm.filter;

import com.epam.gym.crm.context.TransactionContext;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Order(1)
public class TransactionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String txId = UUID.randomUUID().toString();
        TransactionContext.set(txId);
        response.setHeader("X-Transaction-Id", txId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TransactionContext.clear();
        }

    }

}