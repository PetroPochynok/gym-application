package com.epam.gym.crm.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();

        String testSecret = "9a74132a4e214c7784f18b32c69d8d1e2f3g4h5j6k7l8m9n0p1q2r3s4t5u6v7w";
        ReflectionTestUtils.setField(jwtProvider, "secretString", testSecret);

        jwtProvider.init();
    }

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

    @Test
    void generateInternalServiceToken_shouldCreateValidSystemToken() {
        String token = jwtProvider.generateInternalServiceToken();

        assertTrue(jwtProvider.validateToken(token));
        assertEquals("gym-crm-internal", jwtProvider.getUsernameFromToken(token));

        String testSecret = "9a74132a4e214c7784f18b32c69d8d1e2f3g4h5j6k7l8m9n0p1q2r3s4t5u6v7w";
        java.security.Key jwtSecret = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        java.util.List<?> roles = claims.get("roles", java.util.List.class);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_SYSTEM", roles.getFirst().toString());
    }
}