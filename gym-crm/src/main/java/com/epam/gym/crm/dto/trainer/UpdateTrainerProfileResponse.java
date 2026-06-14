package com.epam.gym.crm.dto.trainer;

import com.epam.gym.crm.dto.trainee.TraineeShortResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTrainerProfileResponse {

    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;

    private List<TraineeShortResponse> trainees;
}