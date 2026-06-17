package com.epam.gym.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadResponse;
import com.epam.gym.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerWorkloadService workloadService;

    private TrainerWorkloadRequest request;
    private TrainerWorkloadResponse response;

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .username("olga.k")
                .firstName("Olga")
                .lastName("Kravets")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 5, 18))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        response = TrainerWorkloadResponse.builder()
                .username("olga.k")
                .firstName("Olga")
                .lastName("Kravets")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @Test
    void testHandleWorkload_ShouldReturn200Ok() throws Exception {
        doNothing().when(workloadService).updateWorkload(any(TrainerWorkloadRequest.class));

        mockMvc.perform(post("/api/v1/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(workloadService, times(1)).updateWorkload(any(TrainerWorkloadRequest.class));
    }

    @Test
    void testGetWorkload_WhenTrainerExists_ShouldReturn200AndResponse() throws Exception {
        when(workloadService.getTrainerWorkload("olga.k")).thenReturn(response);

        mockMvc.perform(get("/api/v1/workload/olga.k")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("olga.k"))
                .andExpect(jsonPath("$.firstName").value("Olga"))
                .andExpect(jsonPath("$.lastName").value("Kravets"));

        verify(workloadService, times(1)).getTrainerWorkload("olga.k");
    }

    @Test
    void testGetWorkload_WhenTrainerNotFound_ShouldReturn404NotFound() throws Exception {
        when(workloadService.getTrainerWorkload("unknown")).thenThrow(new IllegalArgumentException("Trainer not found"));

        mockMvc.perform(get("/api/v1/workload/unknown"))
                .andExpect(status().isNotFound());

        verify(workloadService, times(1)).getTrainerWorkload("unknown");
    }
}