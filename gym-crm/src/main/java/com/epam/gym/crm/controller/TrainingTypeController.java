package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.training.TrainingTypeResponse;
import com.epam.gym.crm.mapper.TrainingTypeMapper;
import com.epam.gym.crm.model.TrainingType;
import com.epam.gym.crm.service.TrainingTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Training Type Controller")
@RestController
@RequestMapping("/training-types")
@AllArgsConstructor
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeMapper trainingTypeMapper;

    @ApiOperation(value = "Get all training types", notes = "Returns list of all available training types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success")
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes() {
        List<TrainingType> types = trainingTypeService.getAll();

        return ResponseEntity.ok(trainingTypeMapper.toResponseList(types));
    }
}