package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.trainee.TraineeShortResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private CredentialUtil credentialUtil;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingService trainingService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_shouldGenerateCredentialsAndSave() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");
        trainer.setSpecialization(type);

        when(credentialUtil.generateUsername("John", "Doe"))
                .thenReturn("john.doe");

        Trainer savedTrainer = new Trainer();
        savedTrainer.setFirstName("John");
        savedTrainer.setLastName("Doe");
        savedTrainer.setUsername("john.doe");
        savedTrainer.setActive(true);
        savedTrainer.setSpecialization(type);

        when(trainerRepository.save(any(Trainer.class)))
                .thenReturn(savedTrainer);

        Trainer result = trainerService.create(trainer);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertTrue(result.isActive());
        assertNotNull(result.getSpecialization());
        assertEquals("CARDIO", result.getSpecialization().getTrainingTypeName());

        verify(trainerRepository).save(any(Trainer.class));
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void update_shouldModifyAndSaveTrainer_whenExists() {
        Long id = 1L;

        TrainingType oldType = new TrainingType();
        oldType.setTrainingTypeName("CARDIO");

        Trainer existing = new Trainer();
        existing.setId(id);
        existing.setFirstName("John");
        existing.setLastName("Doe");
        existing.setSpecialization(oldType);
        existing.setActive(true);

        Trainer updatedData = new Trainer();
        updatedData.setFirstName("Mike");
        updatedData.setLastName("Smith");
        updatedData.setActive(false);

        when(trainerRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(i -> i.getArgument(0));

        Trainer result = trainerService.update(id, updatedData);

        assertEquals("Mike", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("CARDIO", result.getSpecialization().getTrainingTypeName());
        assertFalse(result.isActive());

        verify(trainerRepository).findById(id);
        verify(trainerRepository).save(existing);
    }

    @Test
    void update_shouldThrowException_whenTrainerNotFound() {
        Long id = 1L;

        Trainer updatedData = new Trainer();

        when(trainerRepository.findById(id))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> trainerService.update(id, updatedData)
        );

        assertEquals(
                "Trainer not found: id=" + id,
                ex.getMessage()
        );

        verify(trainerRepository).findById(id);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnTrainer_whenExists() {
        Long id = 1L;

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("FITNESS");

        Trainer trainer = new Trainer();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setSpecialization(type);

        when(trainerRepository.findById(id))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getById(id);

        assertTrue(result.isPresent());

        Trainer found = result.get();
        assertEquals("john", found.getUsername());
        assertEquals("FITNESS", found.getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findById(id);
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {
        Long id = 1L;

        when(trainerRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getById(id);

        assertTrue(result.isEmpty());

        verify(trainerRepository).findById(id);
    }

    @Test
    void getAll_shouldReturnListOfTrainers() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("FITNESS");

        Trainer trainer = new Trainer();
        trainer.setUsername("john");
        trainer.setSpecialization(type);

        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<Trainer> result = trainerService.getAll();

        assertEquals(1, result.size());
        assertEquals("john", result.getFirst().getUsername());
        assertEquals("FITNESS", result.getFirst().getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findAll();
    }

    @Test
    void getByUsername_shouldReturnTrainer_whenExists() {
        String username = "john";

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("FITNESS");

        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setSpecialization(type);

        when(trainerRepository.findByUsername(username))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        assertEquals("FITNESS", result.get().getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findByUsername(username);
    }

    @Test
    void getByUsername_shouldReturnEmpty_whenNotFound() {
        String username = "john";

        when(trainerRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getByUsername(username);

        assertTrue(result.isEmpty());

        verify(trainerRepository).findByUsername(username);
    }

    @Test
    void changePassword_shouldUpdatePassword_whenAuthenticated() {
        Trainer trainer = new Trainer();
        trainer.setUsername("john");
        trainer.setPassword("oldPass");

        trainerService.changePassword(trainer, "newPass123");

        assertEquals("newPass123", trainer.getPassword());

        verify(trainerRepository).save(trainer);
    }

    @Test
    void updateTrainerActiveStatus_shouldSetActiveStatus() {
        String username = "john";

        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setActive(true);

        when(trainerRepository.findByUsername(username))
                .thenReturn(Optional.of(trainer));

        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(i -> i.getArgument(0));

        trainerService.updateTrainerActiveStatus(username, false);

        assertFalse(trainer.isActive());

        verify(trainerRepository).findByUsername(username);
        verify(trainerRepository).save(trainer);
    }

    @Test
    void updateTrainerActiveStatus_shouldThrowException_whenTrainerNotFound() {
        String username = "unknown";
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.updateTrainerActiveStatus(username, true));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getNotAssignedTrainers_shouldReturnFilteredList() {

        Trainer t1 = new Trainer();
        t1.setId(1L);
        t1.setUsername("trainer1");

        Trainer t2 = new Trainer();
        t2.setId(2L);
        t2.setUsername("trainer2");

        Training training = new Training();
        training.setTrainer(t1);

        when(trainerRepository.findAll())
                .thenReturn(List.of(t1, t2));

        when(trainingRepository.findByTrainee_Username("trainee"))
                .thenReturn(List.of(training));

        TrainerNotAssignedResponse dto = new TrainerNotAssignedResponse();
        dto.setUsername("trainer2");

        when(trainerMapper.toNotAssignedResponseList(anyList()))
                .thenReturn(List.of(dto));

        List<TrainerNotAssignedResponse> result =
                trainerService.getNotAssignedTrainers("trainee");

        assertEquals(1, result.size());
        assertEquals("trainer2", result.getFirst().getUsername());

        verify(trainerRepository).findAll();
        verify(trainingRepository).findByTrainee_Username("trainee");
        verify(trainerMapper).toNotAssignedResponseList(anyList());
    }

    @Test
    void getNotAssignedTrainers_shouldFilterOutInactiveTrainers() {
        Trainer activeTrainer = new Trainer();
        activeTrainer.setId(1L);
        activeTrainer.setActive(true);

        Trainer inactiveTrainer = new Trainer();
        inactiveTrainer.setId(2L);
        inactiveTrainer.setActive(false);

        when(trainerRepository.findAll()).thenReturn(List.of(activeTrainer, inactiveTrainer));
        when(trainingRepository.findByTrainee_Username("trainee")).thenReturn(Collections.emptyList());
        when(trainerMapper.toNotAssignedResponseList(anyList())).thenReturn(Collections.emptyList());

        trainerService.getNotAssignedTrainers("trainee");

        verify(trainerMapper).toNotAssignedResponseList(argThat(list -> list.size() == 1 && list.contains(activeTrainer)));
    }

    @Test
    void register_shouldReturnTrainerRegistrationResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSpecialization("CARDIO");

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");

        when(credentialUtil.generateUsername("John", "Doe"))
                .thenReturn("john.doe");

        when(credentialUtil.generatePassword())
                .thenReturn("pass");

        when(passwordEncoder.encode("pass"))
                .thenReturn("hashedPass");

        Trainer savedTrainer = new Trainer();
        savedTrainer.setUsername("john.doe");
        savedTrainer.setPassword("hashedPass");

        when(trainingTypeRepository.findByTrainingTypeName("CARDIO"))
                .thenReturn(Optional.of(type));

        when(trainerRepository.save(any(Trainer.class)))
                .thenReturn(savedTrainer);

        when(jwtProvider.generateToken("john.doe"))
                .thenReturn("mock-jwt-token");

        TrainerRegistrationResponse result = trainerService.register(request);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("pass", result.getPassword());
        assertEquals("mock-jwt-token", result.getToken());

        verify(trainingTypeRepository).findByTrainingTypeName("CARDIO");
        verify(trainerRepository).save(any(Trainer.class));
        verify(passwordEncoder).encode("pass");
        verify(jwtProvider).generateToken("john.doe");
    }

    @Test
    void register_shouldThrowException_whenTrainingTypeNotFound() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setSpecialization("UNKNOWN");
        when(trainingTypeRepository.findByTrainingTypeName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.register(request));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getProfile_shouldReturnTrainerProfileResponse() {
        String username = "john";

        Trainer trainer = new Trainer();
        trainer.setUsername(username);

        TrainerProfileResponse response = new TrainerProfileResponse();

        List<TraineeShortResponse> trainees = Collections.emptyList();

        when(trainerRepository.findByUsername(username))
                .thenReturn(Optional.of(trainer));

        when(trainerMapper.toProfileResponse(trainer))
                .thenReturn(response);

        when(trainingService.getTraineesByTrainerUsername(username))
                .thenReturn(trainees);

        TrainerProfileResponse result = trainerService.getProfile(username);

        assertNotNull(result);
        assertEquals(trainees, result.getTrainees());

        verify(trainerRepository).findByUsername(username);
        verify(trainerMapper).toProfileResponse(trainer);
        verify(trainingService).getTraineesByTrainerUsername(username);
    }

    @Test
    void getProfile_shouldThrowException_whenTrainerNotFound() {
        String username = "unknown";
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.getProfile(username));
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void updateProfile_shouldReturnUpdatedResponse() {
        String username = "john";

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");

        Trainer existing = new Trainer();
        existing.setId(1L);
        existing.setUsername(username);
        existing.setSpecialization(type);

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        Trainer mappedEntity = new Trainer();
        mappedEntity.setFirstName(request.getFirstName());
        mappedEntity.setLastName(request.getLastName());
        mappedEntity.setActive(request.getIsActive());

        Trainer saved = new Trainer();
        saved.setId(1L);
        saved.setUsername(username);
        saved.setFirstName(request.getFirstName());
        saved.setLastName(request.getLastName());
        saved.setActive(request.getIsActive());
        saved.setSpecialization(type);

        UpdateTrainerProfileResponse response = new UpdateTrainerProfileResponse();

        List<TraineeShortResponse> trainees = Collections.emptyList();

        when(trainerRepository.findByUsername(username))
                .thenReturn(Optional.of(existing));

        when(trainerMapper.toEntity(request))
                .thenReturn(mappedEntity);

        when(trainerRepository.findById(existing.getId()))
                .thenReturn(Optional.of(existing));

        when(trainerRepository.save(any(Trainer.class)))
                .thenReturn(saved);

        when(trainerMapper.toUpdateProfileResponse(saved))
                .thenReturn(response);

        when(trainingService.getTraineesByTrainerUsername(username))
                .thenReturn(trainees);

        UpdateTrainerProfileResponse result = trainerService.updateProfile(username, request);

        assertNotNull(result);
        assertEquals(trainees, result.getTrainees());

        verify(trainerRepository).findByUsername(username);
        verify(trainerRepository).findById(existing.getId());
        verify(trainerRepository).save(any(Trainer.class));
        verify(trainerMapper).toEntity(request);
        verify(trainerMapper).toUpdateProfileResponse(saved);
        verify(trainingService).getTraineesByTrainerUsername(username);
    }

    @Test
    void updateProfile_shouldThrowException_whenTrainerNotFound() {
        String username = "unknown";
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.updateProfile(username, request));
        verifyNoInteractions(trainerMapper);
    }

}