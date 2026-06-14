package com.epam.gym.crm.mapper;

import com.epam.gym.crm.dto.training.TrainingForTraineeResponse;
import com.epam.gym.crm.dto.training.TrainingForTrainerResponse;
import com.epam.gym.crm.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainer.firstName", target = "trainerName")
    TrainingForTraineeResponse toResponse(Training training);

    List<TrainingForTraineeResponse> toTraineeResponseList(List<Training> trainings);

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainee.firstName", target = "traineeName")
    TrainingForTrainerResponse toTrainerResponse(Training training);

    List<TrainingForTrainerResponse> toTrainerResponseList(List<Training> trainings);
}