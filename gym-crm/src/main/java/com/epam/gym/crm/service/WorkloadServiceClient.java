package com.epam.gym.crm.service;

import com.epam.gym.crm.client.WorkloadClient;
import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkloadServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(WorkloadServiceClient.class);

    private final WorkloadClient workloadClient;

    public WorkloadServiceClient(WorkloadClient workloadClient) {
        this.workloadClient = workloadClient;
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallbackSendWorkloadRequest")
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

        LOG.info("Sending workload update request to gym-workload for trainer: {}", trainer.getUsername());
        workloadClient.updateWorkload(workloadRequest);
        LOG.info("Workload update request successfully processed for trainer: {}", trainer.getUsername());
    }

    public void fallbackSendWorkloadRequest(Training training, ActionType actionType, Throwable t) {
        LOG.error("[CIRCUIT BREAKER FALLBACK] gym-workload service is unavailable! " +
                        "Fallback handling triggered for trainer: {}. Reason: {}",
                training.getTrainer().getUsername(), t.getMessage());
    }
}