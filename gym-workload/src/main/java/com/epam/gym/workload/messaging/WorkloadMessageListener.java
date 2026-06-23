package com.epam.gym.workload.messaging;

import com.epam.gym.workload.dto.TrainerWorkloadRequest;
import com.epam.gym.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadMessageListener {

    private final TrainerWorkloadService workloadService;

    @JmsListener(destination = "${app.queue.trainer-workload}", containerFactory = "jmsListenerContainerFactory")
    public void receiveWorkloadUpdate(@Valid TrainerWorkloadRequest request) {
        log.info("Received asynchronous workload message from ActiveMQ for trainer: {}, Action: {}",
                request.getUsername(), request.getActionType());

        workloadService.updateWorkload(request);
        log.info("Successfully processed message for trainer: {}", request.getUsername());
    }
}