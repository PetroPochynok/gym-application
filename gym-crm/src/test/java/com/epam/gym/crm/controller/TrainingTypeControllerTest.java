package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.training.TrainingTypeResponse;
import com.epam.gym.crm.mapper.TrainingTypeMapper;
import com.epam.gym.crm.model.TrainingType;
import com.epam.gym.crm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {
    @Mock
    private TrainingTypeService trainingTypeService;
    @Mock
    private TrainingTypeMapper trainingTypeMapper;
    @InjectMocks
    private TrainingTypeController trainingTypeController;

    @Test
    void getAllTrainingTypes_shouldReturnMappedList() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");

        TrainingTypeResponse response = new TrainingTypeResponse();
        response.setTrainingTypeName("CARDIO");

        when(trainingTypeService.getAll())
                .thenReturn(List.of(type));

        when(trainingTypeMapper.toResponseList(List.of(type)))
                .thenReturn(List.of(response));

        ResponseEntity<List<TrainingTypeResponse>> result = trainingTypeController.getAllTrainingTypes();

        assertEquals(200, result.getStatusCodeValue());

        List<TrainingTypeResponse> body = result.getBody();

        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("CARDIO", body.getFirst().getTrainingTypeName());

        verify(trainingTypeService).getAll();
        verify(trainingTypeMapper).toResponseList(List.of(type));
    }
}