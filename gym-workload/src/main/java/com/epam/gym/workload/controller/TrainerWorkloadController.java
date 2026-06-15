package com.epam.gym.workload.controller;

import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadResponse;
import com.epam.gym.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/workload")
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadController {

    private final TrainerWorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Void> handleWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        log.info("Received workload request for trainer: {}", request.getUsername());
        workloadService.updateWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(@PathVariable String username) {
        try {
            TrainerWorkloadResponse response = workloadService.getTrainerWorkload(username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Trainer not found: {}", username);
            return ResponseEntity.notFound().build();
        }
    }
}