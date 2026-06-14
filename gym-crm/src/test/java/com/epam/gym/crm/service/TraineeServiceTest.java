package com.epam.gym.crm.service;

import com.epam.gym.crm.dto.trainee.*;
import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import com.epam.gym.crm.exception.NotFoundException;
import com.epam.gym.crm.mapper.TraineeMapper;
import com.epam.gym.crm.mapper.TrainerMapper;
import com.epam.gym.crm.model.Trainee;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.repository.TraineeRepository;
import com.epam.gym.crm.repository.TrainerRepository;
import com.epam.gym.crm.repository.TrainingRepository;
import com.epam.gym.crm.service.security.JwtProvider;
import com.epam.gym.crm.util.CredentialUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private CredentialUtil credentialUtil;
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private TraineeService traineeService;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingService trainingService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void create_shouldGenerateCredentialsAndSaveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        when(credentialUtil.generateUsername("John", "Doe"))
                .thenReturn("john.doe");

        Trainee savedTrainee = new Trainee();
        savedTrainee.setFirstName("John");
        savedTrainee.setLastName("Doe");
        savedTrainee.setUsername("john.doe");
        savedTrainee.setActive(true);

        when(traineeRepository.save(any(Trainee.class)))
                .thenReturn(savedTrainee);

        Trainee result = traineeService.create(trainee);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertTrue(result.isActive());

        verify(credentialUtil).generateUsername("John", "Doe");

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());

        Trainee saved = captor.getValue();
        assertEquals("john.doe", saved.getUsername());
        assertTrue(saved.isActive());
    }

    @Test
    void update_shouldModifyAndSaveTrainee() {
        Long id = 1L;

        Trainee existing = new Trainee();
        existing.setId(id);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setAddress("Old Address");
        existing.setDateOfBirth(LocalDate.of(2000, 1, 1));
        existing.setActive(true);

        Trainee updatedData = new Trainee();
        updatedData.setFirstName("New");
        updatedData.setLastName("Surname");
        updatedData.setAddress("Kyiv");
        updatedData.setDateOfBirth(LocalDate.of(1999, 5, 10));
        updatedData.setActive(false);

        when(traineeRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.update(id, updatedData);

        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("Surname", result.getLastName());
        assertEquals("Kyiv", result.getAddress());
        assertEquals(LocalDate.of(1999, 5, 10), result.getDateOfBirth());
        assertFalse(result.isActive());

        verify(traineeRepository).findById(id);
        verify(traineeRepository).save(existing);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        Long id = 1L;

        when(traineeRepository.findById(id))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> traineeService.update(id, new Trainee())
        );

        assertEquals("Trainee not found: id=1", ex.getMessage());

        verify(traineeRepository).findById(id);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnTrainee_whenExists() {
        Long id = 1L;

        Trainee trainee = new Trainee();
        trainee.setId(id);
        trainee.setUsername("john.doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        when(traineeRepository.findById(id))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getById(id);

        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUsername());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());

        verify(traineeRepository).findById(id);
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {
        Long id = 1L;

        when(traineeRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getById(id);

        assertTrue(result.isEmpty());

        verify(traineeRepository).findById(id);
    }

    @Test
    void getAll_shouldReturnListOfTrainees() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        t1.setUsername("john");

        Trainee t2 = new Trainee();
        t2.setId(2L);
        t2.setUsername("alex");

        when(traineeRepository.findAll())
                .thenReturn(List.of(t1, t2));

        List<Trainee> result = traineeService.getAll();

        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getUsername());
        assertEquals("alex", result.get(1).getUsername());

        verify(traineeRepository).findAll();
    }

    @Test
    void delete_shouldThrowException_whenTraineeDoesNotExist() {
        Long id = 1L;

        when(traineeRepository.existsById(id))
                .thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> traineeService.delete(id)
        );

        assertEquals("Trainee not found: id=1", ex.getMessage());

        verify(traineeRepository).existsById(id);
        verify(traineeRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldCallDeleteById() {
        Long id = 1L;

        when(traineeRepository.existsById(id)).thenReturn(true);

        traineeService.delete(id);

        verify(traineeRepository).existsById(id);
        verify(traineeRepository).deleteById(id);
    }

    @Test
    void getByUsername_shouldReturnTrainee_whenExists() {
        String username = "john.doe";

        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        assertEquals("John", result.get().getFirstName());

        verify(traineeRepository).findByUsername(username);
    }

    @Test
    void getByUsername_shouldReturnEmpty_whenNotFound() {
        String username = "unknown.user";

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getByUsername(username);

        assertTrue(result.isEmpty());

        verify(traineeRepository).findByUsername(username);
    }

    @Test
    void changePassword_shouldUpdateAndSaveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUsername("john");
        trainee.setPassword("oldPass");

        traineeService.changePassword(trainee, "newPass123");

        assertEquals("newPass123", trainee.getPassword());

        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateActiveStatus_shouldSetActiveStatus() {
        Trainee trainee = new Trainee();
        trainee.setActive(false);

        when(traineeRepository.findByUsername("oleh.s"))
                .thenReturn(Optional.of(trainee));

        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(i -> i.getArgument(0));

        traineeService.updateActiveStatus("oleh.s", true);

        assertTrue(trainee.isActive());

        verify(traineeRepository).findByUsername("oleh.s");
        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateActiveStatus_shouldSetActiveStatus_whenTraineeExists() {
        String username = "john";

        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setActive(true);

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.of(trainee));

        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(i -> i.getArgument(0));

        traineeService.updateActiveStatus(username, false);

        assertFalse(trainee.isActive());

        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateActiveStatus_shouldThrowException_whenTraineeNotFound() {
        String username = "john";

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> traineeService.updateActiveStatus(username, true)
        );

        assertEquals(
                "Trainee not found: username=" + username,
                ex.getMessage()
        );

        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void deleteByUsername_shouldDeleteTrainee_whenExists() {
        String username = "john.doe";

        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername(username);

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(username);

        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldThrowException_whenTraineeNotFound() {
        String username = "unknown.user";

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> traineeService.deleteByUsername(username)
        );

        assertEquals("Trainee not found: username=" + username, ex.getMessage());

        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository, never()).delete(any());
    }

    @Test
    void updateTraineeTrainers_shouldReplaceTrainingsAndReturnResponse() {
        String traineeUsername = "john";

        Trainee trainee = new Trainee();
        trainee.setUsername(traineeUsername);

        Trainer trainer = new Trainer();
        trainer.setUsername("trainer1");

        TrainerAssignmentRequest req = new TrainerAssignmentRequest();
        req.setTrainerUsername("trainer1");
        req.setTrainingName("Boxing");
        req.setDuration(60);
        req.setTrainingDate(LocalDate.of(2025, 1, 1));

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainers(List.of(req));

        when(traineeRepository.findByUsername(traineeUsername))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("trainer1"))
                .thenReturn(Optional.of(trainer));

        when(trainerMapper.toShortResponseList(anyList()))
                .thenReturn(List.of(new TrainerShortResponse()));

        UpdateTraineeTrainersResponse response =
                traineeService.updateTraineeTrainers(traineeUsername, request);

        assertNotNull(response);
        assertNotNull(response.getTrainers());

        verify(traineeRepository).findByUsername(traineeUsername);
        verify(trainingRepository).deleteByTrainee_Username(traineeUsername);
        verify(trainingRepository).saveAll(anyList());
        verify(trainerMapper).toShortResponseList(anyList());
    }

    @Test
    void register_shouldCreateTrainee_andReturnResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Kyiv");

        when(credentialUtil.generateUsername("John", "Doe"))
                .thenReturn("john.doe");

        when(credentialUtil.generatePassword())
                .thenReturn("generatedPass");

        when(passwordEncoder.encode("generatedPass"))
                .thenReturn("hashedGeneratedPass");

        Trainee created = new Trainee();
        created.setUsername("john.doe");
        created.setPassword("hashedGeneratedPass");
        created.setActive(true);

        when(traineeRepository.save(any(Trainee.class)))
                .thenReturn(created);

        when(jwtProvider.generateToken("john.doe"))
                .thenReturn("mock-jwt-token");

        TraineeRegistrationResponse response = traineeService.register(request);

        assertNotNull(response);
        assertEquals("john.doe", response.getUsername());
        assertEquals("generatedPass", response.getPassword());
        assertEquals("mock-jwt-token", response.getToken());

        verify(traineeRepository).save(any(Trainee.class));
        verify(jwtProvider).generateToken("john.doe");
    }

    @Test
    void getProfile_shouldReturnProfileResponse() {
        String username = "john";

        Trainee trainee = new Trainee();
        trainee.setUsername(username);

        TraineeProfileResponse response = new TraineeProfileResponse();

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.of(trainee));

        when(traineeMapper.toProfileResponse(trainee))
                .thenReturn(response);

        when(trainingService.getTrainersByTraineeUsername(username))
                .thenReturn(Collections.emptyList());

        TraineeProfileResponse result = traineeService.getProfile(username);

        assertNotNull(result);
        assertEquals(Collections.emptyList(), result.getTrainers());

        verify(traineeRepository).findByUsername(username);
        verify(traineeMapper).toProfileResponse(trainee);
        verify(trainingService).getTrainersByTraineeUsername(username);
    }

    @Test
    void updateProfile_shouldReturnUpdatedResponse() {
        String username = "john";

        Trainee existing = new Trainee();
        existing.setId(1L);
        existing.setUsername(username);

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();

        Trainee mappedEntity = new Trainee();

        Trainee saved = new Trainee();
        saved.setId(1L);
        saved.setUsername(username);

        UpdateTraineeProfileResponse response = new UpdateTraineeProfileResponse();

        List<TrainerShortResponse> trainers = Collections.emptyList();

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.of(existing));

        when(traineeMapper.toEntity(request))
                .thenReturn(mappedEntity);

        when(traineeRepository.findById(existing.getId()))
                .thenReturn(Optional.of(existing));

        when(traineeRepository.save(any(Trainee.class)))
                .thenReturn(saved);

        when(traineeMapper.toUpdateProfileResponse(saved))
                .thenReturn(response);

        when(trainingService.getTrainersByTraineeUsername(username))
                .thenReturn(trainers);

        UpdateTraineeProfileResponse result = traineeService.updateProfile(username, request);

        assertNotNull(result);
        assertEquals(trainers, result.getTrainers());

        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository).findById(existing.getId());
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeMapper).toEntity(request);
        verify(traineeMapper).toUpdateProfileResponse(saved);
        verify(trainingService).getTrainersByTraineeUsername(username);
    }

    @Test
    void updateProfile_shouldThrowException_whenNotFound() {
        String username = "john";

        when(traineeRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();

        assertThrows(NotFoundException.class,
                () -> traineeService.updateProfile(username, request));

        verify(traineeRepository).findByUsername(username);
        verifyNoInteractions(traineeMapper, trainingService);
    }
}