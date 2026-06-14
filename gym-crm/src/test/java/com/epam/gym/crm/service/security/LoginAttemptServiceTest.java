package com.epam.gym.crm.service.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private static final String USERNAME = "john.doe";

    private final LoginAttemptService loginAttemptService = new LoginAttemptService();

    @Test
    void isBlocked_shouldReturnFalseForUserWithoutFailedAttempts() {
        assertFalse(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    void loginFailed_shouldBlockUserAfterThirdFailedAttempt() {
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);

        assertFalse(loginAttemptService.isBlocked(USERNAME));

        loginAttemptService.loginFailed(USERNAME);

        assertTrue(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    void loginSucceeded_shouldClearFailedAttemptsAndUnlockUser() {
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);

        loginAttemptService.loginSucceeded(USERNAME);

        assertFalse(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    void isBlocked_shouldClearExpiredLockAndReturnFalse() {
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> lockTimeCache =
                (ConcurrentHashMap<String, Long>) ReflectionTestUtils.getField(loginAttemptService, "lockTimeCache");

        if (lockTimeCache != null) {
            lockTimeCache.put(USERNAME, System.currentTimeMillis() - 1);
        }

        assertFalse(loginAttemptService.isBlocked(USERNAME));
    }
}