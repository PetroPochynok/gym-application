package com.epam.gym.crm.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider();

    @Test
    void generateToken_shouldCreateValidTokenWithUsernameAsSubject() {
        String username = "john.doe";
        String token = jwtProvider.generateToken(username);

        assertTrue(jwtProvider.validateToken(token));
        assertEquals(username, jwtProvider.getUsernameFromToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        assertFalse(jwtProvider.validateToken("not-a-jwt-token"));
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtProvider.generateToken("john.doe");
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtProvider.validateToken(tamperedToken));
    }
}