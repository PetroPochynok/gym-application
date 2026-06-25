package com.epam.gym.workload.messaging;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ValidationException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @InjectMocks
    private WorkloadMessageListener workloadMessageListener;

    @Test
    void receiveWorkloadUpdate_shouldForwardRequestToWorkloadService() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .username("olga.k")
                .firstName("Olga")
                .lastName("Kravets")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 6, 24))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        workloadMessageListener.receiveWorkloadUpdate(request);

        verify(workloadService, times(1)).updateWorkload(request);
    }

    @Test
    void receiveWorkloadUpdate_shouldThrowException_whenDurationIsNegative() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .username("olga.k")
                .trainingDuration(-60)
                .build();

        assertThrows(ValidationException.class, () ->
                workloadMessageListener.receiveWorkloadUpdate(request)
        );

        verify(workloadService, never()).updateWorkload(any());
    }

    @Test
    void receiveWorkloadUpdate_shouldThrowException_whenUsernameIsMissing() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .username("")
                .trainingDuration(60)
                .build();

        assertThrows(ValidationException.class, () ->
                workloadMessageListener.receiveWorkloadUpdate(request)
        );

        verify(workloadService, never()).updateWorkload(any());
    }
}