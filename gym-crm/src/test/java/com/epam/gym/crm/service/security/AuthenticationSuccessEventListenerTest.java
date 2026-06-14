package com.epam.gym.crm.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationSuccessEventListenerTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthenticationSuccessEventListener listener;

    @Test
    void onApplicationEvent_shouldClearFailedAttemptsForAuthenticatedUsername() {
        String username = "john.doe";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "password");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        listener.onApplicationEvent(event);

        verify(loginAttemptService).loginSucceeded(username);
    }
}