package com.epam.gym.workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "training_month_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingMonthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_year", nullable = false)
    private int year;

    @Column(name = "training_month", nullable = false)
    private int month;

    @Column(nullable = false)
    private int totalWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_workload_id", nullable = false)
    @ToString.Exclude
    private TrainerWorkload trainerWorkload;
}