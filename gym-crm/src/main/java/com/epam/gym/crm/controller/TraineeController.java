package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.trainee.*;
import com.epam.gym.crm.dto.trainer.TrainerNotAssignedResponse;
import com.epam.gym.crm.dto.training.TraineeTrainingFilterRequest;
import com.epam.gym.crm.dto.training.TrainingForTraineeResponse;
import com.epam.gym.crm.mapper.TrainingMapper;
import com.epam.gym.crm.model.Training;
import com.epam.gym.crm.service.TraineeService;
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

@Api(tags = "Trainee Controller")
@RestController
@RequestMapping("/trainees")
@AllArgsConstructor
@Validated
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final TrainerService trainerService;
    private final TrainingMapper trainingMapper;
    private final AuthenticationService authenticationService;

    @ApiOperation(value = "Register new trainee")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Trainee successfully registered"),
            @ApiResponse(code = 400, message = "Validation error")
    })
    @PostMapping
    public TraineeRegistrationResponse register(
            @ApiParam(value = "Trainee registration request", required = true)
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        return traineeService.register(request);
    }

    @ApiOperation(value = "Get trainee profile", notes = "Returns trainee profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @GetMapping("/{username}")
    public TraineeProfileResponse getTraineeProfile(
            @ApiParam(value = "Trainee username (3-50 chars)", required = true, example = "john.doe")
            @PathVariable @NotBlank @Size(min = 3, max = 50) String username
    ) {
        return traineeService.getProfile(username);
    }

    @ApiOperation(value = "Update trainee profile", notes = "Updates trainee profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully updated"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PutMapping("/{username}")
    public UpdateTraineeProfileResponse updateTraineeProfile(
            @ApiParam(value = "Trainee username (3-50 chars)", required = true, example = "john.doe")
            @PathVariable @NotBlank @Size(min = 3, max = 50) String username,

            @ApiParam(value = "Update trainee profile request", required = true)
            @Valid @RequestBody UpdateTraineeProfileRequest request
    ) {
        return traineeService.updateProfile(username, request);
    }

    @ApiOperation(value = "Delete trainee profile", notes = "Deletes trainee profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully deleted"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @ApiParam(value = "Trainee username (3-50 chars)", required = true, example = "john.doe")
            @PathVariable @NotBlank @Size(min = 3, max = 50) String username
    ) {
        traineeService.deleteByUsername(username);

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get not assigned trainers", notes = "Returns trainers that are not assigned to trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/{username}/not-assigned-trainers")
    public List<TrainerNotAssignedResponse> getNotAssignedTrainers(
            @ApiParam(value = "Trainee username", required = true, example = "john.doe")
            @PathVariable @NotBlank String username
    ) {
        return trainerService.getNotAssignedTrainers(username);
    }

    @ApiOperation(value = "Update trainee trainers", notes = "Assigns or updates trainers for a trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainers successfully updated"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PutMapping("/{username}/trainers")
    public UpdateTraineeTrainersResponse updateTraineeTrainers(
            @ApiParam(value = "Trainee username", required = true, example = "john.doe")
            @PathVariable @NotBlank String username,

            @ApiParam(value = "List of trainer assignments", required = true)
            @Valid @RequestBody UpdateTraineeTrainersRequest request
    ) {
        return traineeService.updateTraineeTrainers(username, request);
    }

    @ApiOperation(value = "Get trainee trainings", notes = "Returns trainings for trainee with optional filters")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/{username}/trainings")
    public List<TrainingForTraineeResponse> getTraineeTrainings(
            @ApiParam(value = "Trainee username", required = true, example = "john.doe")
            @PathVariable String username,

            @ApiParam(value = "Optional filters for trainings")
            @ModelAttribute TraineeTrainingFilterRequest filter
    ) {
        List<Training> trainings = trainingService.getTraineeTrainings(username, filter);

        return trainingMapper.toTraineeResponseList(trainings);
    }

    @ApiOperation(value = "Update trainee active status", notes = "Enables or disables trainee account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status successfully updated"),
            @ApiResponse(code = 400, message = "Validation error"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PatchMapping("/active")
    public ResponseEntity<Void> updateActiveStatus(
            @ApiParam(value = "Trainee active status request", required = true)
            @Valid @RequestBody UpdateTraineeActiveRequest request
    ) {
        traineeService.updateActiveStatus(request.getUsername(), request.getIsActive());

        return ResponseEntity.ok().build();
    }
}
