package com.epam.gym.crm.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CredentialUtilTest {

    @Test
    void generateUsernameStatic_returnsBaseWhenFree() {
        String username = CredentialUtil.generateUsernameStatic(
                "John",
                "Doe",
                List.of("jane.doe")
        );

        assertEquals("john.doe", username);
    }

    @Test
    void generateUsernameStatic_appendsNextSuffixWhenBaseTaken() {
        String username = CredentialUtil.generateUsernameStatic(
                "John",
                "Doe",
                List.of("john.doe", "john.doe1", "john.doe4")
        );

        assertEquals("john.doe5", username);
    }

    @Test
    void generateUsernameStatic_handlesMultipleExistingSuffixes() {
        String username = CredentialUtil.generateUsernameStatic(
                "John",
                "Doe",
                List.of("john.doe", "john.doe1", "john.doe2", "john.doe10")
        );

        assertEquals("john.doe11", username);
    }

    @Test
    void generatePasswordStatic_returnsValidPassword() {
        String password = CredentialUtil.generatePasswordStatic();

        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void generatePasswordStatic_isRandom_eachCallDifferent() {
        String p1 = CredentialUtil.generatePasswordStatic();
        String p2 = CredentialUtil.generatePasswordStatic();

        assertNotEquals(p1, p2);
    }
}