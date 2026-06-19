package com.epam.gym.crm.client;

import com.epam.gym.crm.config.FeignClientConfig;
import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import com.epam.gym.crm.dto.workload.TrainerWorkloadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gym-workload", path = "/api/v1/workload", configuration = FeignClientConfig.class)
public interface WorkloadClient {

    @PostMapping
    ResponseEntity<Void> updateWorkload(@RequestBody TrainerWorkloadRequest request);

    @GetMapping("/{username}")
    ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(@PathVariable("username") String username);
}