package com.epam.gym.crm.service.authentication;

import com.epam.gym.crm.model.User;
import com.epam.gym.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_success() {
        String username = "john";
        String password = "password123";
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        when(authenticationManager.authenticate(authToken)).thenReturn(authToken);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = authenticationService.authenticate(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());

        verify(authenticationManager).authenticate(authToken);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void authenticate_invalidCredentials_throwsException() {
        String username = "john";
        String password = "wrong_password";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        when(authenticationManager.authenticate(authToken)).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(username, password));

        verify(authenticationManager).authenticate(authToken);
        verifyNoInteractions(userRepository);
    }

    @Test
    void authenticate_userNotFoundAfterAuth_throwsException() {
        String username = "missing";
        String password = "password123";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        when(authenticationManager.authenticate(authToken)).thenReturn(authToken);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.authenticate(username, password));

        assertEquals("User not found with username: " + username, ex.getMessage());

        verify(authenticationManager).authenticate(authToken);
        verify(userRepository).findByUsername(username);
    }
}