package com.epam.gym.crm.service;

import com.epam.gym.crm.client.WorkloadClient;
import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class WorkloadServiceClientTest {

    @Autowired
    private WorkloadServiceClient workloadServiceClient;

    @MockBean
    private WorkloadClient workloadClient;

    @Test
    void sendWorkloadRequest_shouldCallWorkloadClient_whenServiceIsAvailable() {
        Training training = buildTraining();

        assertDoesNotThrow(() -> workloadServiceClient.sendWorkloadRequest(training, ActionType.ADD));

        verify(workloadClient, times(1)).updateWorkload(any(TrainerWorkloadRequest.class));
    }

    @Test
    void sendWorkloadRequest_shouldTriggerFallback_whenWorkloadClientThrowsException() {
        Training training = buildTraining();

        doThrow(new RuntimeException("Service Unavailable"))
                .when(workloadClient).updateWorkload(any(TrainerWorkloadRequest.class));

        assertDoesNotThrow(() -> workloadServiceClient.sendWorkloadRequest(training, ActionType.ADD));

        verify(workloadClient, times(1)).updateWorkload(any(TrainerWorkloadRequest.class));
    }

    private Training buildTraining() {
        Trainer trainer = new Trainer();
        trainer.setUsername("olga.k");
        trainer.setFirstName("Olga");
        trainer.setLastName("K");
        trainer.setActive(true);

        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainingDate(LocalDate.now());
        training.setDuration(60);
        return training;
    }
}