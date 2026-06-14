package com.epam.gym.crm.dto.training;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingForTrainerResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer duration;
    private String traineeName;
}
