package com.epam.gym.workload.controller;

import com.epam.gym.workload.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainerWorkloadControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void getWorkload_withoutToken_shouldReturn403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/workload/trainer.olga"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getWorkload_withInvalidToken_shouldReturn401Unauthorized() throws Exception {
        when(jwtProvider.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/workload/trainer.olga")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized()); // Очікуємо 401 від нашого JwtAuthFilter
    }

    @Test
    void getWorkload_withValidToken_shouldPassSecurity() throws Exception {
        when(jwtProvider.validateToken(anyString())).thenReturn(true);
        when(jwtProvider.getUsernameFromToken(anyString())).thenReturn("trainer.olga");

        mockMvc.perform(get("/api/v1/workload/trainer.olga")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }
}