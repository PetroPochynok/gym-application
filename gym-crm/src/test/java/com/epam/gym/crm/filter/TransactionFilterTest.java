package com.epam.gym.crm.filter;

import com.epam.gym.crm.context.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFilterTest {

    private final TransactionFilter filter = new TransactionFilter();

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void shouldSetTransactionIdInContextAndHeaderThenClearIt() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            String currentTxId = TransactionContext.get();
            assertNotNull(currentTxId);
            assertFalse(currentTxId.isEmpty());
        };

        filter.doFilter(request, response, filterChain);

        assertNull(TransactionContext.get());

        String headerTxId = response.getHeader("X-Transaction-Id");
        assertNotNull(headerTxId);
        assertFalse(headerTxId.isEmpty());
    }
}