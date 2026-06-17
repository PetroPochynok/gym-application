package com.epam.gym.workload.util;

public class TransactionContext {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String transactionId) {
        CONTEXT.set(transactionId);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}