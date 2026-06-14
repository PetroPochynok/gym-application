package com.epam.gym.crm.service;

import com.epam.gym.crm.model.TrainingType;
import com.epam.gym.crm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAll_shouldReturnListOfTrainingTypes() {
        List<TrainingType> expectedTypes = List.of(new TrainingType(), new TrainingType());
        when(trainingTypeRepository.findAll()).thenReturn(expectedTypes);

        List<TrainingType> actualTypes = trainingTypeService.getAll();

        assertEquals(expectedTypes.size(), actualTypes.size());
        verify(trainingTypeRepository).findAll();
    }
}