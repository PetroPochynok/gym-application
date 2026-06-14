package com.epam.gym.crm.dto.trainee;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class UpdateTraineeTrainersRequest {

    @NotEmpty
    private List<TrainerAssignmentRequest> trainers;
}