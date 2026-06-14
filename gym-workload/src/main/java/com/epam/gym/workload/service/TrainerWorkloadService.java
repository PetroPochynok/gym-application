package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.model.TrainerWorkload;
import com.epam.gym.workload.model.TrainingMonthSummary;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository workloadRepository;

    @Transactional
    public void updateWorkload(TrainerWorkloadRequest request) {
        log.info("Processing workload update for trainer: {}, Action: {}", request.getUsername(), request.getActionType());

        TrainerWorkload trainerWorkload = workloadRepository.findByUsername(request.getUsername())
                .orElseGet(() -> createNewTrainerWorkload(request));

        trainerWorkload.setActive(request.getIsActive());

        LocalDate date = request.getTrainingDate();
        int year = date.getYear();
        int month = date.getMonthValue();

        Optional<TrainingMonthSummary> existingSummary = trainerWorkload.getMonthSummaries().stream()
                .filter(summary -> summary.getYear() == year && summary.getMonth() == month)
                .findFirst();

        if (request.getActionType() == ActionType.ADD) {
            if (existingSummary.isPresent()) {
                TrainingMonthSummary summary = existingSummary.get();
                summary.setTotalWorkingHours(summary.getTotalWorkingHours() + request.getTrainingDuration());
            } else {
                TrainingMonthSummary newSummary = TrainingMonthSummary.builder()
                        .year(year)
                        .month(month)
                        .totalWorkingHours(request.getTrainingDuration())
                        .trainerWorkload(trainerWorkload)
                        .build();
                trainerWorkload.getMonthSummaries().add(newSummary);
            }
        } else if (request.getActionType() == ActionType.DELETE) {
            if (existingSummary.isPresent()) {
                TrainingMonthSummary summary = existingSummary.get();
                int newDuration = summary.getTotalWorkingHours() - request.getTrainingDuration();

                if (newDuration <= 0) {
                    trainerWorkload.getMonthSummaries().remove(summary);
                } else {
                    summary.setTotalWorkingHours(newDuration);
                }
            } else {
                log.warn("Attempted to delete training duration for non-existing month summary. Trainer: {}, Year: {}, Month: {}",
                        trainerWorkload.getUsername(), year, month);
            }
        }

        workloadRepository.save(trainerWorkload);
        log.info("Successfully updated workload for trainer: {}", request.getUsername());
    }

    private TrainerWorkload createNewTrainerWorkload(TrainerWorkloadRequest request) {
        log.info("Trainer not found. Creating new workload record for username: {}", request.getUsername());
        return TrainerWorkload.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.getIsActive())
                .build();
    }
}