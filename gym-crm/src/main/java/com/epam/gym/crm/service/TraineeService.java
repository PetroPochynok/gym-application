package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.trainee.*;
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
import com.epam.gym.crm.service.security.JwtProvider;
import com.epam.gym.crm.util.CredentialUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TraineeService {

    private static final Logger LOG = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final CredentialUtil credentialUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Trainee create(Trainee trainee) {
        String username = credentialUtil.generateUsername(trainee.getFirstName(), trainee.getLastName());
        trainee.setUsername(username);
        trainee.setActive(true);

        Trainee savedTrainee = traineeRepository.save(trainee);

        LOG.info("Trainee created: id={}, username={}, firstName={}, lastName={}, dateOfBirth={}, address={}, active={}",
                savedTrainee.getId(), savedTrainee.getUsername(), savedTrainee.getFirstName(),
                savedTrainee.getLastName(), savedTrainee.getDateOfBirth(), savedTrainee.getAddress(),
                savedTrainee.isActive());

        return savedTrainee;
    }

    public Trainee update(Long id, Trainee updatedData) {
        Trainee existing = traineeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: id=%d", id)));

        existing.setFirstName(updatedData.getFirstName());
        existing.setLastName(updatedData.getLastName());
        existing.setDateOfBirth(updatedData.getDateOfBirth());
        existing.setAddress(updatedData.getAddress());
        existing.setActive(updatedData.isActive());

        Trainee updatedTrainee = traineeRepository.save(existing);

        LOG.info("Trainee updated: id={}, username={}, firstName={}, lastName={}, dateOfBirth={}, address={}, active={}",
                updatedTrainee.getId(), updatedTrainee.getUsername(), updatedTrainee.getFirstName(),
                updatedTrainee.getLastName(), updatedTrainee.getDateOfBirth(), updatedTrainee.getAddress(),
                updatedTrainee.isActive());

        return updatedTrainee;
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getById(Long id) {
        Optional<Trainee> trainee = traineeRepository.findById(id);

        LOG.debug("Trainee lookup executed: id={}", id);

        return trainee;
    }

    @Transactional(readOnly = true)
    public List<Trainee> getAll() {
        List<Trainee> trainees = traineeRepository.findAll();

        LOG.debug("Retrieved all trainees: count={}", trainees.size());

        return trainees;
    }

    public void delete(Long id) {
        if (!traineeRepository.existsById(id)) {
            throw new NotFoundException(String.format("Trainee not found: id=%d", id));
        }

        traineeRepository.deleteById(id);

        LOG.info("Trainee deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getByUsername(String username) {
        Optional<Trainee> trainee = traineeRepository.findByUsername(username);

        LOG.debug("Trainee lookup executed by username={}", username);

        return trainee;
    }

    public void changePassword(Trainee trainee, String newPassword) {
        trainee.setPassword(newPassword);

        traineeRepository.save(trainee);

        LOG.info("Trainee password changed: username={}", trainee.getUsername());
    }

    @Transactional
    public void updateActiveStatus(String username, Boolean isActive) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", username)));

        trainee.setActive(isActive);

        traineeRepository.save(trainee);

        LOG.info("Trainee active status updated: username={}, isActive={}",
                username, isActive);
    }

    public void deleteByUsername(String traineeUsername) {
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", traineeUsername)));

        traineeRepository.delete(trainee);

        LOG.info("Trainee deleted by username: id={}, username={}", trainee.getId(), traineeUsername);
    }

    @Transactional
    public UpdateTraineeTrainersResponse updateTraineeTrainers(String traineeUsername, UpdateTraineeTrainersRequest request) {
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", traineeUsername)));

        trainingService.deleteByTraineeUsername(traineeUsername);

        List<Training> trainings = request.getTrainers().stream()
                .map(dto -> {
                    Trainer trainer = trainerRepository.findByUsername(dto.getTrainerUsername())
                            .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: username=%s", dto.getTrainerUsername())));

                    Training training = new Training();
                    training.setTrainee(trainee);
                    training.setTrainer(trainer);
                    training.setTrainingType(trainer.getSpecialization());
                    training.setTrainingName(dto.getTrainingName());
                    training.setTrainingDate(dto.getTrainingDate());
                    training.setDuration(dto.getDuration());

                    return training;
                })
                .toList();

        List<Training> savedTrainings = trainingRepository.saveAll(trainings);

        for (Training savedTraining : savedTrainings) {
            trainingService.sendWorkloadRequest(savedTraining, ActionType.ADD);
        }

        UpdateTraineeTrainersResponse response = new UpdateTraineeTrainersResponse();
        response.setTrainers(
                trainerMapper.toShortResponseList(trainings.stream()
                        .map(Training::getTrainer)
                        .toList())
        );

        return response;
    }

    public TraineeRegistrationResponse register(TraineeRegistrationRequest request) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.getFirstName());
        trainee.setLastName(request.getLastName());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        String rawPassword = credentialUtil.generatePassword();
        trainee.setPassword(passwordEncoder.encode(rawPassword));
        Trainee created = create(trainee);

        String token = jwtProvider.generateToken(created.getUsername());

        return new TraineeRegistrationResponse(created.getUsername(), rawPassword, token);
    }

    public TraineeProfileResponse getProfile(String username) {
        Trainee trainee = getByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", username)));

        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        response.setTrainers(trainingService.getTrainersByTraineeUsername(username));

        return response;
    }

    public UpdateTraineeProfileResponse updateProfile(String username, UpdateTraineeProfileRequest request) {
        Trainee existing = getByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainee not found: username=%s", username)));

        Trainee updated = traineeMapper.toEntity(request);

        Trainee saved = update(existing.getId(), updated);

        UpdateTraineeProfileResponse response = traineeMapper.toUpdateProfileResponse(saved);

        response.setTrainers(trainingService.getTrainersByTraineeUsername(saved.getUsername()));

        return response;
    }
}