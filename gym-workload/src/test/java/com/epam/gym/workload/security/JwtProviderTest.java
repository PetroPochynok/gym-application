package com.epam.gym.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private Key testKey;
    private final String secretString = "9a74132a4e214c7784f18b32c69d8d1e2f3g4h5j6k7l8m9n0p1q2r3s4t5u6v7w";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secretString", secretString);
        jwtProvider.init();

        this.testKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    private String createTestToken(String username, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(testKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = createTestToken("trainer.olga", 3600000);
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String username = "trainer.olga";
        String token = createTestToken(username, 3600000);
        assertEquals(username, jwtProvider.getUsernameFromToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        assertFalse(jwtProvider.validateToken("invalid-token-string"));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Токен, який закінчився 5 хвилин тому (-300000 мс)
        String expiredToken = createTestToken("trainer.olga", -300000);
        assertFalse(jwtProvider.validateToken(expiredToken));
    }
}