package com.epam.gym.crm.repository;

import com.epam.gym.crm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {
    List<Training> findByTrainee_Username(String username);
    List<Training> findByTrainer_Username(String username);
    void deleteByTrainee_Username(String username);
}