package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.training.CreateTrainingRequest;
import com.epam.gym.crm.service.TrainingService;
import com.epam.gym.crm.service.authentication.AuthenticationService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Training Controller")
@RestController
@RequestMapping("/trainings")
@AllArgsConstructor
@Validated
public class TrainingController {

    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    @ApiOperation(value = "Create training", notes = "Creates a new training session")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training successfully created"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @ApiParam(value = "Training creation request", required = true)
            @Valid @RequestBody CreateTrainingRequest request
    ) {
        trainingService.create(request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        trainingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}