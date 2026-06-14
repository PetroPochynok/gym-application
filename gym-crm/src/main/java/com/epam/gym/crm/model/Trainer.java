package com.epam.gym.crm.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Trainer extends User {

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;
}