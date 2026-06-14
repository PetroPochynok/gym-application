package com.epam.gym.crm.controller;

import com.epam.gym.crm.dto.auth.ChangePasswordRequest;
import com.epam.gym.crm.dto.auth.JwtResponse;
import com.epam.gym.crm.model.Trainee;
import com.epam.gym.crm.model.Trainer;
import com.epam.gym.crm.model.User;
import com.epam.gym.crm.service.TraineeService;
import com.epam.gym.crm.service.TrainerService;
import com.epam.gym.crm.service.authentication.AuthenticationService;
import com.epam.gym.crm.service.security.JwtProvider;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Api(tags = "Auth Controller")
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Validated
public class AuthController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;
    private final JwtProvider jwtProvider;

    @ApiOperation(value = "Login user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully authenticated"),
            @ApiResponse(code = 400, message = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @ApiParam(value = "Username", required = true)
            @RequestHeader("authUsername") @NotBlank String username,

            @ApiParam(value = "Password", required = true)
            @RequestHeader("password") @NotBlank String password
    ) {
        authenticationService.authenticate(username, password);
        String token = jwtProvider.generateToken(username);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @ApiOperation(value = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Password changed successfully"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 400, message = "Validation error")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @ApiParam(value = "Change password request", required = true)
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = authenticationService.authenticate(request.getUsername(), request.getOldPassword());

        if (user instanceof Trainee trainee) {
            traineeService.changePassword(trainee, request.getNewPassword());
        } else if (user instanceof Trainer trainer) {
            trainerService.changePassword(trainer, request.getNewPassword());
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Logout user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged out")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}