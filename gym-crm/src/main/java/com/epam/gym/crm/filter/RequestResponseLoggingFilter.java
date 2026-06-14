package com.epam.gym.crm.filter;

import com.epam.gym.crm.context.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Order(2)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String txId = TransactionContext.get();

        LOG.info("[txId={}] Incoming request: {} {}",
                txId,
                request.getMethod(),
                request.getRequestURI()
        );

        try {
            filterChain.doFilter(request, response);

            LOG.info("[txId={}] Response status: {}",
                    txId,
                    response.getStatus()
            );
        } catch (Exception e) {
            LOG.error("[txId={}] Error: {}",
                    txId,
                    e.getMessage()
            );
            throw e;
        }
    }

}
