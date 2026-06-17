package com.epam.gym.crm.service;

import com.epam.gym.crm.client.WorkloadClient;
import com.epam.gym.crm.dto.trainee.TraineeShortResponse;
import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import com.epam.gym.crm.dto.training.CreateTrainingRequest;
import com.epam.gym.crm.dto.training.TraineeTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainerTrainingFilterRequest;
import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import com.epam.gym.crm.exception.NotFoundException;
import com.epam.gym.crm.mapper.TraineeMapper;
import com.epam.gym.crm.mapper.TrainerMapper;
import com.epam.gym.crm.model.*;
import com.epam.gym.crm.repository.TraineeRepository;
import com.epam.gym.crm.repository.TrainerRepository;
import com.epam.gym.crm.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private WorkloadServiceClient workloadServiceClient;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_savesTraining_successfully() {
        CreateTrainingRequest request = new CreateTrainingRequest();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");
        request.setTrainingName("Morning");
        request.setTrainingDate(LocalDate.of(2026, 5, 18));
        request.setTrainingDuration(60);

        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("trainee1");

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("trainer1");
        trainer.setSpecialization(new TrainingType());

        Training savedTraining = new Training();
        savedTraining.setId(100L);
        savedTraining.setTrainee(trainee);
        savedTraining.setTrainer(trainer);
        savedTraining.setTrainingName("Morning");
        savedTraining.setTrainingDate(request.getTrainingDate());
        savedTraining.setDuration(60);

        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);

        Training result = trainingService.create(request);

        assertNotNull(result);
        assertEquals("Morning", result.getTrainingName());
        verify(workloadServiceClient, times(1)).sendWorkloadRequest(any(Training.class), eq(ActionType.ADD));
    }

    @Test
    void create_throwsNotFoundException_whenTraineeNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest();
        request.setTraineeUsername("unknown_trainee");

        when(traineeRepository.findByUsername("unknown_trainee")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.create(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFoundException_whenTrainerNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("unknown_trainer");

        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findByUsername("unknown_trainer")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.create(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void delete_removesTraining_successfully() {
        Training training = buildTraining();
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        assertDoesNotThrow(() -> trainingService.delete(1L));

        verify(trainingRepository, times(1)).delete(training);
        verify(workloadServiceClient, times(1)).sendWorkloadRequest(any(Training.class), eq(ActionType.DELETE));
    }

    @Test
    void delete_throwsNotFoundException_whenTrainingNotFound() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.delete(99L));
        verify(trainingRepository, never()).delete(any());
    }

    @Test
    void deleteByTraineeUsername_deletesAllTrainingsForTrainee() {
        Training training1 = buildTraining();
        Training training2 = buildTraining();
        List<Training> trainings = List.of(training1, training2);

        when(trainingRepository.findByTrainee_Username("trainee.user")).thenReturn(trainings);

        trainingService.deleteByTraineeUsername("trainee.user");

        verify(trainingRepository, times(2)).delete(any(Training.class));
        verify(workloadServiceClient, times(2)).sendWorkloadRequest(any(Training.class), eq(ActionType.DELETE));
    }

    @Test
    void getById_notFound() {
        when(trainingRepository.findById(9L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.getById(9L);

        assertTrue(result.isEmpty());
        verify(trainingRepository).findById(9L);
    }

    @Test
    void getById_found() {
        Training training = buildTraining();
        training.setId(1L);

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(trainingRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnListOfTrainings() {
        Training training = buildTraining();
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<Training> result = trainingService.getAll();

        assertEquals(1, result.size());
        verify(trainingRepository).findAll();
    }

    @Test
    void getTraineeTrainings_returnsList() {
        Training training = buildTraining();
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any())).thenReturn(List.of(training));

        TraineeTrainingFilterRequest filter = new TraineeTrainingFilterRequest();

        List<Training> result = trainingService.getTraineeTrainings("trainee.user", filter);

        assertEquals(1, result.size());
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTrainerTrainings_returnsList() {
        Training training = buildTraining();
        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any())).thenReturn(List.of(training));

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest();

        List<Training> result = trainingService.getTrainerTrainings("trainer.user", filter);

        assertEquals(1, result.size());
        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTrainersByTraineeUsername_returnsMappedShortResponses() {
        Training training = buildTraining();
        when(trainingRepository.findByTrainee_Username("john")).thenReturn(List.of(training));
        when(trainerMapper.toShortResponseList(anyList())).thenReturn(List.of(new TrainerShortResponse()));

        List<TrainerShortResponse> result = trainingService.getTrainersByTraineeUsername("john");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainerMapper).toShortResponseList(anyList());
    }

    @Test
    void getTraineesByTrainerUsername_returnsMappedShortResponses() {
        Training training = buildTraining();
        when(trainingRepository.findByTrainer_Username("mike")).thenReturn(List.of(training));
        when(traineeMapper.toShortResponseList(anyList())).thenReturn(List.of(new TraineeShortResponse()));

        List<TraineeShortResponse> result = trainingService.getTraineesByTrainerUsername("mike");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(traineeMapper).toShortResponseList(anyList());
    }

    private Training buildTraining() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("trainee.user");

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("trainer.user");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");

        Training training = new Training();
        training.setId(10L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName("Morning");
        training.setTrainingDate(LocalDate.of(2024, 1, 1));
        training.setDuration(60);

        return training;
    }
}