package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.trainer.*;
import com.epam.gym.crm.dto.training.TrainerTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainingForTrainerResponse;
import com.epam.gym.crm.dto.workload.TrainerWorkloadResponse;
import com.epam.gym.crm.mapper.TrainingMapper;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.service.TrainerService;
import com.epam.gym.crm.service.TrainingService;
import com.epam.gym.crm.service.authentication.AuthenticationService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Api(tags = "Trainer Controller")
@RestController
@RequestMapping("/trainers")
@AllArgsConstructor
@Validated
public class TrainerController {

    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;
    private final TrainingService trainingService;
    private final TrainingMapper trainingMapper;

    @ApiOperation(value = "Register trainer", notes = "Creates a new trainer account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer successfully registered"),
            @ApiResponse(code = 400, message = "Validation error")
    })
    @PostMapping
    public TrainerRegistrationResponse register(
            @ApiParam(value = "Trainer registration request", required = true)
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        return trainerService.register(request);
    }

    @ApiOperation(value = "Get trainer profile", notes = "Returns trainer profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/{username}")
    public TrainerProfileResponse getTrainerProfile(
            @ApiParam(value = "Trainer username", required = true, example = "john.trainer")
            @PathVariable @NotBlank @Size(min = 3, max = 50) String username
    ) {
        return trainerService.getProfile(username);
    }


    @ApiOperation(value = "Update trainer profile", notes = "Updates trainer profile information by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully updated"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PutMapping("/{username}")
    public UpdateTrainerProfileResponse updateTrainerProfile(
            @ApiParam(value = "Trainer username", required = true, example = "john.trainer")
            @PathVariable @NotBlank String username,

            @ApiParam(value = "Trainer profile update request", required = true)
            @Valid @RequestBody UpdateTrainerProfileRequest request
    ) {
        return trainerService.updateProfile(username, request);
    }

    @ApiOperation(value = "Get trainer trainings", notes = "Returns trainings for trainer with optional filters")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/{username}/trainings")
    public List<TrainingForTrainerResponse> getTrainerTrainings(
            @ApiParam(value = "Trainer username", required = true, example = "john.trainer")
            @PathVariable String username,

            @ApiParam(value = "Optional training filters (query params)")
            @ModelAttribute TrainerTrainingFilterRequest filter
    ) {
        List<Training> trainings = trainingService.getTrainerTrainings(username, filter);

        return trainingMapper.toTrainerResponseList(trainings);
    }

    @ApiOperation(value = "Update trainer active status", notes = "Enables or disables trainer account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status successfully updated"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PatchMapping("/active")
    public ResponseEntity<Void> updateTrainerActiveStatus(
            @ApiParam(value = "Trainer active status request", required = true)
            @Valid @RequestBody UpdateTrainerActiveRequest request
    ) {
        trainerService.updateTrainerActiveStatus(request.getUsername(), request.getIsActive());

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get trainer workload summary", notes = "Retrieves working hours summary from workload microservice")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/{username}/workload")
    public TrainerWorkloadResponse getTrainerWorkload(
            @ApiParam(value = "Trainer username", required = true, example = "john.trainer")
            @PathVariable @NotBlank String username
    ) {
        return trainerService.getTrainerWorkload(username);
    }

}
