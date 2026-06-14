package com.epam.gym.crm.service.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT = 3;
    private static final long BLOCK_DURATION = TimeUnit.MINUTES.toMillis(5);

    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockTimeCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        lockTimeCache.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0);
        attempts++;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPT) {
            lockTimeCache.put(username, System.currentTimeMillis() + BLOCK_DURATION);
        }
    }

    public boolean isBlocked(String username) {
        if (!lockTimeCache.containsKey(username)) {
            return false;
        }

        long lockExpiration = lockTimeCache.get(username);
        if (System.currentTimeMillis() > lockExpiration) {
            attemptsCache.remove(username);
            lockTimeCache.remove(username);
            return false;
        }

        return true;
    }
}