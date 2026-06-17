package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadResponse;
import com.epam.gym.workload.dto.YearSummaryDto;
import com.epam.gym.workload.model.TrainerWorkload;
import com.epam.gym.workload.model.TrainingMonthSummary;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks
    private TrainerWorkloadService workloadService;

    private TrainerWorkloadRequest request;
    private TrainerWorkload existingWorkload;

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .username("olga.k")
                .firstName("Olga")
                .lastName("Kravets")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 5, 18))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        existingWorkload = TrainerWorkload.builder()
                .id(1L)
                .username("olga.k")
                .firstName("Olga")
                .lastName("Kravets")
                .isActive(true)
                .monthSummaries(new ArrayList<>())
                .build();
    }

    @Test
    void testGetTrainerWorkload_WhenTrainerExists_ShouldReturnCorrectResponse() {
        TrainingMonthSummary summary1 = TrainingMonthSummary.builder()
                .year(2026)
                .month(5)
                .totalWorkingHours(120)
                .trainerWorkload(existingWorkload)
                .build();

        TrainingMonthSummary summary2 = TrainingMonthSummary.builder()
                .year(2025)
                .month(12)
                .totalWorkingHours(90)
                .trainerWorkload(existingWorkload)
                .build();

        existingWorkload.getMonthSummaries().add(summary1);
        existingWorkload.getMonthSummaries().add(summary2);

        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        TrainerWorkloadResponse response = workloadService.getTrainerWorkload("olga.k");

        assertNotNull(response);
        assertEquals("olga.k", response.getUsername());
        assertEquals("Olga", response.getFirstName());
        assertTrue(response.getIsActive());
        assertEquals(2, response.getYears().size());

        YearSummaryDto year2026 = response.getYears().stream()
                .filter(y -> y.getYear() == 2026)
                .findFirst()
                .orElseThrow();
        assertEquals(1, year2026.getMonths().size());
        assertEquals(5, year2026.getMonths().getFirst().getMonth());
        assertEquals(120, year2026.getMonths().getFirst().getTrainingSummaryDuration());
    }

    @Test
    void testGetTrainerWorkload_WhenTrainerDoesNotExist_ShouldThrowException() {
        when(workloadRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> workloadService.getTrainerWorkload("unknown"));
    }

    @Test
    void testUpdateWorkload_WhenTrainerDoesNotExist_ShouldCreateNewTrainerAndSummary() {
        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.empty());

        workloadService.updateWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(workloadRepository, times(1)).save(captor.capture());

        TrainerWorkload savedWorkload = captor.getValue();
        assertEquals("olga.k", savedWorkload.getUsername());
        assertEquals("Olga", savedWorkload.getFirstName());
        assertEquals(1, savedWorkload.getMonthSummaries().size());

        TrainingMonthSummary summary = savedWorkload.getMonthSummaries().getFirst();
        assertEquals(2026, summary.getYear());
        assertEquals(5, summary.getMonth());
        assertEquals(60, summary.getTotalWorkingHours());
    }

    @Test
    void testUpdateWorkload_WhenTrainerExistsButMonthDoesNot_ShouldAddNewMonthSummary() {
        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        workloadService.updateWorkload(request);

        verify(workloadRepository, times(1)).save(existingWorkload);
        assertEquals(1, existingWorkload.getMonthSummaries().size());

        TrainingMonthSummary summary = existingWorkload.getMonthSummaries().getFirst();
        assertEquals(2026, summary.getYear());
        assertEquals(5, summary.getMonth());
        assertEquals(60, summary.getTotalWorkingHours());
    }

    @Test
    void testUpdateWorkload_WhenTrainerAndMonthExist_ShouldIncrementDuration() {
        TrainingMonthSummary existingSummary = new TrainingMonthSummary();
        existingSummary.setYear(2026);
        existingSummary.setMonth(5);
        existingSummary.setTotalWorkingHours(100);
        existingSummary.setTrainerWorkload(existingWorkload);
        existingWorkload.getMonthSummaries().add(existingSummary);

        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        workloadService.updateWorkload(request);

        verify(workloadRepository, times(1)).save(existingWorkload);
        assertEquals(1, existingWorkload.getMonthSummaries().size());
        assertEquals(160, existingSummary.getTotalWorkingHours());
    }

    @Test
    void testUpdateWorkload_WhenActionIsDeleteAndMonthExists_ShouldDecrementDuration() {
        request.setActionType(ActionType.DELETE);

        TrainingMonthSummary existingSummary = new TrainingMonthSummary();
        existingSummary.setYear(2026);
        existingSummary.setMonth(5);
        existingSummary.setTotalWorkingHours(100);
        existingSummary.setTrainerWorkload(existingWorkload);
        existingWorkload.getMonthSummaries().add(existingSummary);

        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        workloadService.updateWorkload(request);

        verify(workloadRepository, times(1)).save(existingWorkload);
        assertEquals(40, existingSummary.getTotalWorkingHours());
    }

    @Test
    void testUpdateWorkload_WhenActionIsDeleteAndNewDurationIsZeroOrLess_ShouldRemoveMonthSummary() {
        request.setActionType(ActionType.DELETE);
        request.setTrainingDuration(100);

        TrainingMonthSummary existingSummary = new TrainingMonthSummary();
        existingSummary.setYear(2026);
        existingSummary.setMonth(5);
        existingSummary.setTotalWorkingHours(100);
        existingSummary.setTrainerWorkload(existingWorkload);
        existingWorkload.getMonthSummaries().add(existingSummary);

        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        workloadService.updateWorkload(request);

        verify(workloadRepository, times(1)).save(existingWorkload);
        assertTrue(existingWorkload.getMonthSummaries().isEmpty());
    }

    @Test
    void testUpdateWorkload_WhenActionIsDeleteButMonthDoesNotExist_ShouldLogWarningAndNotSave() {
        request.setActionType(ActionType.DELETE);

        when(workloadRepository.findByUsername("olga.k")).thenReturn(Optional.of(existingWorkload));

        workloadService.updateWorkload(request);

        verify(workloadRepository, times(1)).save(existingWorkload);
    }
}