package com.epam.gym.crm.service; // або твій пакет, де він лежить

import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest; // перевір цей імпорт
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkloadServiceClient {

    private final JmsTemplate jmsTemplate;
    private final String destinationQueue;

    public WorkloadServiceClient(JmsTemplate jmsTemplate, @Value("${app.queue.trainer-workload}") String destinationQueue) {
        this.jmsTemplate = jmsTemplate;
        this.destinationQueue = destinationQueue;
    }

    public void sendWorkloadRequest(Training training, ActionType actionType) {
        Trainer trainer = training.getTrainer();

        TrainerWorkloadRequest workloadRequest = TrainerWorkloadRequest.builder()
                .username(trainer.getUsername())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .isActive(trainer.isActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getDuration())
                .actionType(actionType)
                .build();

        log.info("Sending asynchronous workload update to ActiveMQ queue '{}' for trainer: {}",
                destinationQueue, trainer.getUsername());

        jmsTemplate.convertAndSend(destinationQueue, workloadRequest);

        log.info("Workload update message successfully placed in queue for trainer: {}", trainer.getUsername());
    }
}