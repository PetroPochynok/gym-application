package com.epam.gym.crm.dto.trainee;

import com.epam.gym.crm.dto.trainer.TrainerShortResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTraineeTrainersResponse {

    private List<TrainerShortResponse> trainers;
}