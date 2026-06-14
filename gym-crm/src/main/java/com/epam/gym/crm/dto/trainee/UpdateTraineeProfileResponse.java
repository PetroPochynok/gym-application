package com.epam.gym.crm.dto.trainee;

import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateTraineeProfileResponse {

    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;

    private List<TrainerShortResponse> trainers;
}