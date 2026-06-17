package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.training.CreateTrainingRequest;
import com.epam.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    @Mock
    private TrainingService trainingService;
    @InjectMocks
    private TrainingController trainingController;

    @Test
    void addTraining_shouldCallService_andReturnOk() {
        CreateTrainingRequest request = new CreateTrainingRequest();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");
        request.setTrainingName("Morning");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);

        ResponseEntity<Void> response = trainingController.addTraining(request);

        assertEquals(200, response.getStatusCodeValue());

        verify(trainingService).create(request);
    }

    @Test
    void deleteTraining_shouldCallService_andReturnNoContent() {
        Long id = 1L;

        ResponseEntity<Void> response = trainingController.deleteTraining(id);

        assertEquals(204, response.getStatusCode().value());
        verify(trainingService).delete(id);
    }
}