package com.epam.gym.crm.mapper;

import com.epam.gym.crm.dto.trainee.TraineeProfileResponse;
import com.epam.gym.crm.dto.trainee.TraineeShortResponse;
import com.epam.gym.crm.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.gym.crm.dto.trainee.UpdateTraineeProfileResponse;
import com.epam.gym.crm.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(source = "active", target = "isActive")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(source = "active", target = "isActive")
    UpdateTraineeProfileResponse toUpdateProfileResponse(Trainee trainee);

    @Mapping(source = "isActive", target = "active")
    Trainee toEntity(UpdateTraineeProfileRequest request);

    TraineeShortResponse toShortResponse(Trainee trainee);

    List<TraineeShortResponse> toShortResponseList(List<Trainee> trainees);
}