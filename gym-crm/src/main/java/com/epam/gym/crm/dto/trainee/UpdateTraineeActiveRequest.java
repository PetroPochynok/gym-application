package com.epam.gym.crm.dto.trainee;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateTraineeActiveRequest {

    @NotBlank
    private String username;

    @NotNull
    private Boolean isActive;
}