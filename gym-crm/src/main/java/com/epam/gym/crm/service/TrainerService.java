package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.trainer.*;
import com.epam.gym.crm.exception.NotFoundException;
import com.epam.gym.crm.mapper.TrainerMapper;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.model.TrainingType;
import com.epam.gym.crm.repository.TrainerRepository;
import com.epam.gym.crm.repository.TrainingRepository;
import com.epam.gym.crm.repository.TrainingTypeRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TrainerService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingService trainingService;
    private final CredentialUtil credentialUtil;
    private final TrainingRepository trainingRepository;
    private final TrainerMapper trainerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Trainer create(Trainer trainer) {
        String username = credentialUtil.generateUsername(trainer.getFirstName(), trainer.getLastName());
        trainer.setUsername(username);
        trainer.setSpecialization(trainer.getSpecialization());
        trainer.setActive(true);

        Trainer savedTrainer = trainerRepository.save(trainer);

        LOG.info("Trainer created: id={}, username={}, firstName={}, lastName={}, specialization={}, active={}",
                savedTrainer.getId(), savedTrainer.getUsername(), savedTrainer.getFirstName(),
                savedTrainer.getLastName(), savedTrainer.getSpecialization(), savedTrainer.isActive());

        return savedTrainer;
    }

    public Trainer update(Long id, Trainer updatedData) {
        Trainer existing = trainerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: id=%d", id)));

        existing.setFirstName(updatedData.getFirstName());
        existing.setLastName(updatedData.getLastName());
        existing.setActive(updatedData.isActive());

        Trainer updatedTrainer = trainerRepository.save(existing);

        LOG.info("Trainer updated: id={}, username={}, firstName={}, lastName={}, specialization={}, active={}",
                updatedTrainer.getId(), updatedTrainer.getUsername(), updatedTrainer.getFirstName(),
                updatedTrainer.getLastName(),  updatedTrainer.getSpecialization().getTrainingTypeName(), updatedTrainer.isActive());

        return updatedTrainer;
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getById(Long id) {
        Optional<Trainer> trainer = trainerRepository.findById(id);

        LOG.debug("Trainer lookup executed: id={}", id);

        return trainer;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAll() {
        List<Trainer> trainers = trainerRepository.findAll();

        LOG.debug("Retrieved all trainers: count={}", trainers.size());

        return trainers;
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getByUsername(String username) {
        Optional<Trainer> trainer = trainerRepository.findByUsername(username);

        LOG.debug("Trainer lookup executed by username={}", username);

        return trainer;
    }

    public void changePassword(Trainer trainer, String newPassword) {
        trainer.setPassword(newPassword);

        trainerRepository.save(trainer);

        LOG.info("Trainer password changed: username={}", trainer.getUsername());
    }

    @Transactional
    public void updateTrainerActiveStatus(String username, Boolean isActive) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: username=%s", username)));

        trainer.setActive(isActive);

        trainerRepository.save(trainer);

        LOG.info("Trainer active status updated: username={}, isActive={}",
                username, isActive);
    }

    @Transactional(readOnly = true)
    public List<TrainerNotAssignedResponse> getNotAssignedTrainers(String traineeUsername) {
        List<Trainer> allTrainers = trainerRepository.findAll();
        List<Training> trainings = trainingRepository.findByTrainee_Username(traineeUsername);

        Set<Long> assignedTrainerIds = trainings.stream()
                .map(t -> t.getTrainer().getId())
                .collect(Collectors.toSet());

        List<Trainer> notAssignedTrainers = allTrainers.stream()
                .filter(Trainer::isActive)
                .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                .toList();

        LOG.debug("Retrieved not-assigned trainers for traineeUsername={}: count={}",
                traineeUsername, notAssignedTrainers.size());

        return trainerMapper.toNotAssignedResponseList(notAssignedTrainers);
    }

    public TrainerRegistrationResponse register(TrainerRegistrationRequest request) {
        TrainingType type = trainingTypeRepository
                .findByTrainingTypeName(request.getSpecialization())
                .orElseThrow(() -> new NotFoundException(String.format("Training type not found: type=%s", request.getSpecialization())));

        Trainer trainer = new Trainer();
        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setSpecialization(type);
        trainer.setActive(true);

        String rawPassword = credentialUtil.generatePassword();
        trainer.setPassword(passwordEncoder.encode(rawPassword));

        Trainer created = create(trainer);

        String token = jwtProvider.generateToken(created.getUsername());

        return new TrainerRegistrationResponse(created.getUsername(), rawPassword, token);
    }

    public TrainerProfileResponse getProfile(String username) {
        Trainer trainer = getByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: username=%s", username)));

        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        response.setTrainees(trainingService.getTraineesByTrainerUsername(username));

        return response;
    }

    public UpdateTrainerProfileResponse updateProfile(String username, UpdateTrainerProfileRequest request) {
        Trainer existing = getByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Trainer not found: username=%s", username)));

        Trainer updated = trainerMapper.toEntity(request);

        Trainer saved = update(existing.getId(), updated);

        UpdateTrainerProfileResponse response = trainerMapper.toUpdateProfileResponse(saved);

        response.setTrainees(trainingService.getTraineesByTrainerUsername(saved.getUsername()));

        return response;
    }
}