package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class WorkloadServiceClientTest {

    @Autowired
    private WorkloadServiceClient workloadServiceClient;

    @MockBean
    private JmsTemplate jmsTemplate;

    @Value("${app.queue.trainer-workload}")
    private String destinationQueue;

    @Test
    void sendWorkloadRequest_shouldSendMessageToActiveMQQueue() {
        Training training = buildTraining();
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);

        workloadServiceClient.sendWorkloadRequest(training, ActionType.ADD);

        verify(jmsTemplate, times(1)).convertAndSend(eq(destinationQueue), requestCaptor.capture());

        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertEquals("olga.k", capturedRequest.getUsername());
        assertEquals("Olga", capturedRequest.getFirstName());
        assertEquals("K", capturedRequest.getLastName());
        assertTrue(capturedRequest.getIsActive());
        assertEquals(training.getTrainingDate(), capturedRequest.getTrainingDate());
        assertEquals(60, capturedRequest.getTrainingDuration());
        assertEquals(ActionType.ADD, capturedRequest.getActionType());
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