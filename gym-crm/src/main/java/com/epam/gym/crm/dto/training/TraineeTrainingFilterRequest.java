package com.epam.gym.crm.dto.training;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TraineeTrainingFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodTo;

    private String trainerName;

    private String trainingType;
}
