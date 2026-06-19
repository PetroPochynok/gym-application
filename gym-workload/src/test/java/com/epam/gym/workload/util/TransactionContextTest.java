package com.epam.gym.workload.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionContextTest {

    @BeforeEach
    void setUp() {
        TransactionContext.clear();
    }

    @Test
    void transactionContext_shouldSetGetAndClearCorrectly() {
        String txId = "test-id-999";

        assertNull(TransactionContext.get());

        TransactionContext.set(txId);
        assertEquals(txId, TransactionContext.get());

        TransactionContext.clear();
        assertNull(TransactionContext.get());
    }
}