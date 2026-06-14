package com.epam.gym.crm.dto.trainee;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class TrainerAssignmentRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainingName;

    @NotNull
    private Integer duration;

    @NotNull
    private LocalDate trainingDate;
}