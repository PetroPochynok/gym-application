package com.epam.gym.crm.mapper;

import com.epam.gym.crm.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.gym.crm.dto.trainer.*;
import com.epam.gym.crm.model.Trainee;
import com.epam.gym.crm.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerShortResponse toShortResponse(Trainer trainer);

    List<TrainerShortResponse> toShortResponseList(List<Trainer> trainers);

    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    @Mapping(source = "active", target = "isActive")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    @Mapping(source = "active", target = "isActive")
    UpdateTrainerProfileResponse toUpdateProfileResponse(Trainer trainer);

    @Mapping(source = "isActive", target = "active")
    Trainer toEntity(UpdateTrainerProfileRequest request);

    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerNotAssignedResponse toNotAssignedResponse(Trainer trainer);

    List<TrainerNotAssignedResponse> toNotAssignedResponseList(List<Trainer> trainers);
}