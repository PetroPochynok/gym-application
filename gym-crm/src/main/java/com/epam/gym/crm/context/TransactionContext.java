package com.epam.gym.crm.context;

public class TransactionContext {
    private static final ThreadLocal<String> TX_ID = new ThreadLocal<>();

    public static void set(String txId) {
        TX_ID.set(txId);
    }

    public static String get() {
        return TX_ID.get();
    }

    public static void clear() {
        TX_ID.remove();
    }

}
