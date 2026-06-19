package com.epam.gym.crm.dto.workload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingMonthSummaryResponse {
    private Long id;
    private int year;
    private int month;
    private int totalWorkingHours;
}