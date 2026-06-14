package com.epam.gym.crm.mapper;

import com.epam.gym.crm.dto.training.TrainingTypeResponse;
import com.epam.gym.crm.model.TrainingType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {
    TrainingTypeResponse toResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toResponseList(List<TrainingType> types);
}