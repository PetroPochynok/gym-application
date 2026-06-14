package com.epam.gym.crm.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainerRegistrationResponse {
    private final String username;
    private final String password;
    private String token;
}
