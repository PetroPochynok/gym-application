package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.trainer.*;
import com.epam.gym.crm.dto.training.TrainerTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainingForTrainerResponse;
import com.epam.gym.crm.mapper.TrainingMapper;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.service.TrainerService;
import com.epam.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TrainerController trainerController;

    @Test
    void register_shouldReturnResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSpecialization("CARDIO");

        TrainerRegistrationResponse expected = new TrainerRegistrationResponse("john.doe", "pass123", "mock-jwt-token");

        when(trainerService.register(request))
                .thenReturn(expected);

        TrainerRegistrationResponse result =
                trainerController.register(request);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("pass123", result.getPassword());
        assertEquals("mock-jwt-token", result.getToken());

        verify(trainerService).register(request);
    }

    @Test
    void getTrainerProfile_shouldReturnProfile() {
        String username = "john";

        TrainerProfileResponse expected = new TrainerProfileResponse();
        expected.setFirstName("John");

        when(trainerService.getProfile(username))
                .thenReturn(expected);

        TrainerProfileResponse result = trainerController.getTrainerProfile(username);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());

        verify(trainerService).getProfile(username);
    }

    @Test
    void updateTrainerProfile_shouldReturnResponse() {
        String username = "john";

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();

        UpdateTrainerProfileResponse expected = new UpdateTrainerProfileResponse();
        expected.setUsername(username);

        when(trainerService.updateProfile(username, request))
                .thenReturn(expected);

        UpdateTrainerProfileResponse result = trainerController.updateTrainerProfile(username, request);

        assertNotNull(result);
        assertEquals(username, result.getUsername());

        verify(trainerService).updateProfile(username, request);
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        String username = "john";

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest();
        filter.setTraineeName("trainee1");

        Training training = new Training();
        training.setTrainingName("Morning");

        TrainingForTrainerResponse response = new TrainingForTrainerResponse();
        response.setTrainingName("Morning");

        when(trainingService.getTrainerTrainings(username, filter))
                .thenReturn(List.of(training));

        when(trainingMapper.toTrainerResponseList(List.of(training)))
                .thenReturn(List.of(response));

        List<TrainingForTrainerResponse> result =
                trainerController.getTrainerTrainings(username, filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning", result.getFirst().getTrainingName());

        verify(trainingService).getTrainerTrainings(username, filter);
        verify(trainingMapper).toTrainerResponseList(List.of(training));
    }

    @Test
    void updateTrainerActiveStatus_shouldCallService_andReturnOk() {
        UpdateTrainerActiveRequest request = new UpdateTrainerActiveRequest();
        request.setUsername("john");
        request.setIsActive(false);

        ResponseEntity<Void> response = trainerController.updateTrainerActiveStatus(request);

        assertEquals(200, response.getStatusCodeValue());

        verify(trainerService).updateTrainerActiveStatus("john", false);
    }
}