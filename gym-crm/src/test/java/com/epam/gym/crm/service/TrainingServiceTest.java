package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.training.CreateTrainingRequest;
import com.epam.gym.crm.dto.training.TraineeTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainerTrainingFilterRequest;
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

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_savesTraining() {

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

        Training savedTraining = new Training();
        savedTraining.setId(100L);
        savedTraining.setTrainee(trainee);
        savedTraining.setTrainer(trainer);
        savedTraining.setTrainingName("Morning");
        savedTraining.setTrainingDate(request.getTrainingDate());
        savedTraining.setDuration(60);

        when(traineeRepository.findByUsername("trainee1"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("trainer1"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class)))
                .thenReturn(savedTraining);

        Training result = trainingService.create(request);

        assertNotNull(result);
        assertEquals("Morning", result.getTrainingName());
        assertEquals(60, result.getDuration());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());

        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainingRepository).save(any(Training.class));
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

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Morning", result.get().getTrainingName());

        verify(trainingRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnListOfTrainings() {
        Training training = buildTraining();

        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<Training> result = trainingService.getAll();

        assertEquals(1, result.size());
        assertEquals("Morning", result.getFirst().getTrainingName());
        assertEquals(60, result.getFirst().getDuration());

        verify(trainingRepository).findAll();
    }

    @Test
    void getTraineeTrainings_returnsList() {
        Training training = buildTraining();

        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of(training));

        TraineeTrainingFilterRequest filter = new TraineeTrainingFilterRequest();
        filter.setPeriodFrom(LocalDate.now().minusDays(1));
        filter.setPeriodTo(LocalDate.now());
        filter.setTrainerName("trainer.user");
        filter.setTrainingType("CARDIO");

        List<Training> result = trainingService.getTraineeTrainings("trainee.user", filter);

        assertEquals(1, result.size());
        assertEquals("Morning", result.getFirst().getTrainingName());
        assertEquals(60, result.getFirst().getDuration());

        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    @Test
    void getTrainerTrainings_returnsList() {
        Training training = buildTraining();

        when(trainingRepository.findAll(ArgumentMatchers.<Specification<Training>>any()))
                .thenReturn(List.of(training));

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest();
        filter.setPeriodFrom(LocalDate.now().minusDays(1));
        filter.setPeriodTo(LocalDate.now());
        filter.setTraineeName("trainee.user");
        filter.setTrainingType("CARDIO");

        List<Training> result = trainingService.getTrainerTrainings("trainer.user", filter);

        assertEquals(1, result.size());
        assertEquals("Morning", result.getFirst().getTrainingName());
        assertEquals(60, result.getFirst().getDuration());

        verify(trainingRepository).findAll(ArgumentMatchers.<Specification<Training>>any());
    }

    private Training buildTraining() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("trainee.user");

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("trainer.user");

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