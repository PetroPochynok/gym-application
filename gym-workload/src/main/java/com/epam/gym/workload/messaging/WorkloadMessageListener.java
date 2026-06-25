package com.epam.gym.workload.messaging;

import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadMessageListener {

    private final TrainerWorkloadService workloadService;

    @JmsListener(destination = "${app.queue.trainer-workload}", containerFactory = "jmsListenerContainerFactory")
    public void receiveWorkloadUpdate(@Payload TrainerWorkloadRequest request) {
        log.info("Received asynchronous workload message from ActiveMQ for trainer: {}", request.getUsername());

        if (request.getTrainingDuration() == null || request.getTrainingDuration() <= 0) {
            log.error("Validation failed! Duration cannot be negative or zero. Provided: {}", request.getTrainingDuration());
            throw new ValidationException("JMS Message Validation failed: Duration must be positive.");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ValidationException("JMS Message Validation failed: Username is missing");
        }

        workloadService.updateWorkload(request);
        log.info("Successfully processed message for trainer: {}", request.getUsername());
    }
}