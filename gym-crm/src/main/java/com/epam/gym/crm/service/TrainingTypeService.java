package com.epam.gym.crm.service;

import com.epam.gym.crm.model.TrainingType;
import com.epam.gym.crm.repository.TrainingTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public List<TrainingType> getAll() {
        return trainingTypeRepository.findAll();
    }
}
