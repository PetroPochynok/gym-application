package com.epam.gym.crm.client;

import com.epam.gym.crm.dto.workload.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gym-workload", path = "/api/v1/workload")
public interface WorkloadClient {

    @PostMapping
    ResponseEntity<Void> updateWorkload(@RequestBody TrainerWorkloadRequest request);
}