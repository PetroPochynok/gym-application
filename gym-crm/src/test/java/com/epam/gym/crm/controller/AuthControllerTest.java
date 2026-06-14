package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.auth.ChangePasswordRequest;
import com.epam.gym.crm.dto.auth.JwtResponse;
import com.epam.gym.crm.model.Trainee;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.service.TraineeService;
import com.epam.gym.crm.service.TrainerService;
import com.epam.gym.crm.service.authentication.AuthenticationService;
import com.epam.gym.crm.service.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldAuthenticate_andReturnJwtToken() {
        String username = "user";
        String password = "pass";
        String fakeToken = "mocked-jwt-token";

        when(jwtProvider.generateToken(username)).thenReturn(fakeToken);

        ResponseEntity<JwtResponse> response = authController.login(username, password);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(fakeToken, response.getBody().getToken());

        verify(authenticationService).authenticate(username, password);
        verify(jwtProvider).generateToken(username);
    }

    @Test
    void changePassword_shouldCallTraineeService_whenUserIsTrainee() {
        Trainee trainee = new Trainee();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("user");
        request.setOldPassword("old");
        request.setNewPassword("new");

        when(authenticationService.authenticate("user", "old"))
                .thenReturn(trainee);

        ResponseEntity<Void> response = authController.changePassword(request);

        assertEquals(200, response.getStatusCodeValue());

        verify(traineeService).changePassword(trainee, "new");
        verify(trainerService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldCallTrainerService_whenUserIsTrainer() {
        Trainer trainer = new Trainer();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("user");
        request.setOldPassword("old");
        request.setNewPassword("new");

        when(authenticationService.authenticate("user", "old"))
                .thenReturn(trainer);

        ResponseEntity<Void> response = authController.changePassword(request);

        assertEquals(200, response.getStatusCodeValue());

        verify(trainerService).changePassword(trainer, "new");
        verify(traineeService, never()).changePassword(any(), any());
    }
}