package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.trainee.*;
import com.epam.gym.crm.dto.trainer.TrainerNotAssignedResponse;
import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import com.epam.gym.crm.dto.training.TraineeTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainingForTraineeResponse;
import com.epam.gym.crm.mapper.TrainingMapper;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.service.TraineeService;
import com.epam.gym.crm.service.TrainerService;
import com.epam.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TraineeControllerTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TraineeController traineeController;

    @Test
    void register_shouldReturnResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Kyiv");

        TraineeRegistrationResponse expected = new TraineeRegistrationResponse("john.doe", "pass123", "mock-jwt-token");

        when(traineeService.register(request))
                .thenReturn(expected);

        TraineeRegistrationResponse result = traineeController.register(request);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("pass123", result.getPassword());
        assertEquals("mock-jwt-token", result.getToken());

        verify(traineeService).register(request);
    }

    @Test
    void getTraineeProfile_shouldReturnProfile() {
        String username = "john";

        TraineeProfileResponse expected = new TraineeProfileResponse();
        expected.setFirstName("John");

        when(traineeService.getProfile(username))
                .thenReturn(expected);

        TraineeProfileResponse result = traineeController.getTraineeProfile(username);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());

        verify(traineeService).getProfile(username);
    }

    @Test
    void updateTraineeProfile_shouldReturnResponse() {
        String username = "john";

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();

        UpdateTraineeProfileResponse expected = new UpdateTraineeProfileResponse();
        expected.setUsername(username);

        when(traineeService.updateProfile(username, request))
                .thenReturn(expected);

        UpdateTraineeProfileResponse result = traineeController.updateTraineeProfile(username, request);

        assertNotNull(result);
        assertEquals(username, result.getUsername());

        verify(traineeService).updateProfile(username, request);
    }

    @Test
    void deleteTraineeProfile_shouldCallService_andReturnOk() {
        String username = "john";

        ResponseEntity<Void> response = traineeController.deleteTraineeProfile(username);

        assertEquals(200, response.getStatusCodeValue());

        verify(traineeService).deleteByUsername(username);
    }

    @Test
    void getNotAssignedTrainers_shouldReturnList() {
        String username = "john";

        TrainerNotAssignedResponse dto = new TrainerNotAssignedResponse();
        dto.setUsername("trainer1");

        List<TrainerNotAssignedResponse> expected = List.of(dto);

        when(trainerService.getNotAssignedTrainers(username))
                .thenReturn(expected);

        List<TrainerNotAssignedResponse> result = traineeController.getNotAssignedTrainers(username);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("trainer1", result.getFirst().getUsername());

        verify(trainerService).getNotAssignedTrainers(username);
    }

    @Test
    void updateTraineeTrainers_shouldReturnResponse() {
        String username = "john";

        TrainerAssignmentRequest assignment = new TrainerAssignmentRequest();
        assignment.setTrainerUsername("trainer1");
        assignment.setTrainingName("Morning");
        assignment.setDuration(60);
        assignment.setTrainingDate(LocalDate.now());

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainers(List.of(assignment));

        TrainerShortResponse trainerShort = new TrainerShortResponse();
        trainerShort.setUsername("trainer1");

        UpdateTraineeTrainersResponse expected = new UpdateTraineeTrainersResponse();
        expected.setTrainers(List.of(trainerShort));

        when(traineeService.updateTraineeTrainers(username, request))
                .thenReturn(expected);

        UpdateTraineeTrainersResponse result = traineeController.updateTraineeTrainers(username, request);

        assertNotNull(result);
        assertEquals(1, result.getTrainers().size());
        assertEquals("trainer1", result.getTrainers().getFirst().getUsername());

        verify(traineeService).updateTraineeTrainers(username, request);
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        String username = "john";

        TraineeTrainingFilterRequest filter = new TraineeTrainingFilterRequest();
        filter.setTrainerName("trainer1");

        Training training = new Training();
        training.setTrainingName("Morning");

        TrainingForTraineeResponse response = new TrainingForTraineeResponse();
        response.setTrainingName("Morning");

        when(trainingService.getTraineeTrainings(username, filter))
                .thenReturn(List.of(training));

        when(trainingMapper.toTraineeResponseList(List.of(training)))
                .thenReturn(List.of(response));

        List<TrainingForTraineeResponse> result = traineeController.getTraineeTrainings(username, filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning", result.getFirst().getTrainingName());

        verify(trainingService).getTraineeTrainings(username, filter);
        verify(trainingMapper).toTraineeResponseList(List.of(training));
    }

    @Test
    void updateActiveStatus_shouldCallService_andReturnOk() {
        UpdateTraineeActiveRequest request = new UpdateTraineeActiveRequest();
        request.setUsername("john");
        request.setIsActive(true);

        ResponseEntity<Void> response =
                traineeController.updateActiveStatus(request);

        assertEquals(200, response.getStatusCodeValue());

        verify(traineeService).updateActiveStatus("john", true);
    }
}