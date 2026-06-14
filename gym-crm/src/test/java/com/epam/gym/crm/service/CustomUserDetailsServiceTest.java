package com.epam.gym.crm.service;

import com.epam.gym.crm.model.User;
import com.epam.gym.crm.repository.UserRepository;
import com.epam.gym.crm.service.security.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final String USERNAME = "john.doe";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldThrowLockedExceptionWhenUserIsBlocked() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(true);

        LockedException exception = assertThrows(LockedException.class,
                () -> customUserDetailsService.loadUserByUsername(USERNAME));

        assertEquals("User is blocked due to 3 unsuccessful login attempts. Try again in 5 minutes.", exception.getMessage());
        verify(userRepository, never()).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(USERNAME));

        assertEquals("User not found with username: john.doe", exception.getMessage());
    }

    @Test
    void loadUserByUsername_shouldReturnEnabledUserDetailsWithRoleUser() {
        User user = createUser(true);
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(USERNAME);

        assertEquals(USERNAME, userDetails.getUsername());
        assertEquals(ENCODED_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority())));

        verify(loginAttemptService, times(2)).isBlocked(USERNAME);
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUserDetailsWhenUserIsInactive() {
        User user = createUser(false);
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(USERNAME);

        assertFalse(userDetails.isEnabled());
        verify(loginAttemptService, times(2)).isBlocked(USERNAME);
    }

    private User createUser(boolean active) {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(ENCODED_PASSWORD);
        user.setActive(active);
        return user;
    }
}