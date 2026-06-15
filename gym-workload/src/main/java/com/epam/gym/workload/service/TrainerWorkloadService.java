package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.*;
import com.epam.gym.workload.model.TrainerWorkload;
import com.epam.gym.workload.model.TrainingMonthSummary;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository workloadRepository;

    @Transactional(readOnly = true)
    public TrainerWorkloadResponse getTrainerWorkload(String username) {
        log.info("Fetching workload summary for trainer: {}", username);

        TrainerWorkload trainerWorkload = workloadRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer workload record not found for username: " + username));

        List<YearSummaryDto> yearsList = trainerWorkload.getMonthSummaries().stream()
                .collect(Collectors.groupingBy(TrainingMonthSummary::getYear))
                .entrySet().stream()
                .map(entry -> {
                    int year = entry.getKey();
                    List<MonthSummaryDto> months = entry.getValue().stream()
                            .map(m -> MonthSummaryDto.builder()
                                    .month(m.getMonth())
                                    .trainingSummaryDuration(m.getTotalWorkingHours())
                                    .build())
                            .collect(Collectors.toList());

                    return YearSummaryDto.builder()
                            .year(year)
                            .months(months)
                            .build();
                })
                .collect(Collectors.toList());

        return TrainerWorkloadResponse.builder()
                .username(trainerWorkload.getUsername())
                .firstName(trainerWorkload.getFirstName())
                .lastName(trainerWorkload.getLastName())
                .isActive(trainerWorkload.isActive())
                .years(yearsList)
                .build();
    }

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