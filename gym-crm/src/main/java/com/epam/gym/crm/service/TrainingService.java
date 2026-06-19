package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.trainee.TraineeShortResponse;
import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import com.epam.gym.crm.dto.training.CreateTrainingRequest;
import com.epam.gym.crm.dto.training.TraineeTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainerTrainingFilterRequest;
import com.epam.gym.crm.dto.workload.ActionType;
import com.epam.gym.crm.exception.NotFoundException;
import com.epam.gym.crm.mapper.TraineeMapper;
import com.epam.gym.crm.mapper.TrainerMapper;
import com.epam.gym.crm.model.Trainee;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.repository.TraineeRepository;
import com.epam.gym.crm.repository.TrainerRepository;
import com.epam.gym.crm.repository.TrainingRepository;
import com.epam.gym.crm.repository.specification.TrainingSpecification;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TrainingService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerMapper trainerMapper;
    private final TraineeMapper traineeMapper;
    private final WorkloadServiceClient workloadServiceClient;

    @Transactional
    public Training create(CreateTrainingRequest request) {

        Trainee trainee = traineeRepository.findByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", request.getTraineeUsername())));

        Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: username=%s", request.getTrainerUsername())));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.getTrainingName());
        training.setTrainingDate(request.getTrainingDate());
        training.setDuration(request.getTrainingDuration());
        training.setTrainingType(trainer.getSpecialization());

        Training savedTraining = trainingRepository.save(training);

        LOG.info("Training created: id={}, traineeName=id:{}, trainerName=id:{}, name={}, date={}, duration={}min",
                savedTraining.getId(),
                savedTraining.getTrainee().getId(),
                savedTraining.getTrainer().getId(),
                savedTraining.getTrainingName(),
                savedTraining.getTrainingDate(),
                savedTraining.getDuration()
        );

        workloadServiceClient.sendWorkloadRequest(savedTraining, ActionType.ADD);

        return savedTraining;
    }

    @Transactional
    public void delete(Long id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Training not found: id=%d", id)));

        trainingRepository.delete(training);

        LOG.info("Training deleted from CRM: id={}, trainerUsername={}", id, training.getTrainer().getUsername());

        workloadServiceClient.sendWorkloadRequest(training, ActionType.DELETE);
    }

    @Transactional
    public void deleteByTraineeUsername(String traineeUsername) {
        List<Training> trainingsToDelete = trainingRepository.findByTrainee_Username(traineeUsername);
        LocalDate today = LocalDate.now();

        for (Training training : trainingsToDelete) {
            trainingRepository.delete(training);

            if (!training.getTrainingDate().isBefore(today)) {
                workloadServiceClient.sendWorkloadRequest(training, ActionType.DELETE);
            } else {
                LOG.info("Past training (id={}) deleted from CRM, workload update skipped to preserve historical data.", training.getId());
            }
        }

        LOG.info("Bulk training deletion executed for trainee: {}, total deleted: {}", traineeUsername, trainingsToDelete.size());
    }

    @Transactional(readOnly = true)
    public Optional<Training> getById(Long id) {
        Optional<Training> training = trainingRepository.findById(id);

        LOG.debug("Training lookup executed: id={}", id);

        return training;
    }

    @Transactional(readOnly = true)
    public List<Training> getAll() {
        List<Training> trainings = trainingRepository.findAll();

        LOG.debug("Retrieved all trainings: count={}", trainings.size());

        return trainings;
    }

    public List<Training> getTraineeTrainings(String traineeUsername, TraineeTrainingFilterRequest filter) {
        Specification<Training> spec = TrainingSpecification.filter(
                traineeUsername,
                filter.getPeriodFrom(),
                filter.getPeriodTo(),
                filter.getTrainerName(),
                filter.getTrainingType()
        );

        List<Training> trainings = trainingRepository.findAll(spec);

        LOG.debug("Retrieved trainee trainings: traineeUsername={}, from={}, to={}, trainerUsername={}, trainingType={}, count={}",
                traineeUsername,
                filter.getPeriodFrom(),
                filter.getPeriodTo(),
                filter.getTrainerName(),
                filter.getTrainingType(),
                trainings.size());

        return trainings;
    }

    public List<Training> getTrainerTrainings(String trainerUsername, TrainerTrainingFilterRequest filter) {
        Specification<Training> spec = TrainingSpecification.filter(
                null,
                filter.getPeriodFrom(),
                filter.getPeriodTo(),
                filter.getTraineeName(),
                filter.getTrainingType()
        );

        List<Training> trainings = trainingRepository.findAll(spec);

        LOG.debug("Retrieved trainer trainings: trainerUsername={}, from={}, to={}, traineeName={}, trainingType={}, count={}",
                trainerUsername,
                filter.getPeriodFrom(),
                filter.getPeriodTo(),
                filter.getTraineeName(),
                filter.getTrainingType(),
                trainings.size());

        return trainings;
    }

    public List<TrainerShortResponse> getTrainersByTraineeUsername(String traineeUsername) {
        List<Training> trainings = trainingRepository.findByTrainee_Username(traineeUsername);

        return trainerMapper.toShortResponseList(
                trainings.stream()
                        .map(Training::getTrainer)
                        .distinct()
                        .toList()
        );
    }

    public List<TraineeShortResponse> getTraineesByTrainerUsername(String trainerUsername) {
        List<Training> trainings = trainingRepository.findByTrainer_Username(trainerUsername);

        return traineeMapper.toShortResponseList(
                trainings.stream()
                        .map(Training::getTrainee)
                        .distinct()
                        .toList()
        );
    }
}
