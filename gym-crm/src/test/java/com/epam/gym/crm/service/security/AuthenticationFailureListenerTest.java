package com.epam.gym.crm.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationFailureListenerTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthenticationFailureListener listener;

    @Test
    void onApplicationEvent_shouldRecordFailedLoginForAuthenticatedUsername() {
        String username = "john.doe";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "bad-password");
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        listener.onApplicationEvent(event);

        verify(loginAttemptService).loginFailed(username);
    }
}