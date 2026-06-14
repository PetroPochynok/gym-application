package com.epam.gym.crm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraineeRegistrationResponse {
    private final String username;
    private final String password;
    private String token;
}
