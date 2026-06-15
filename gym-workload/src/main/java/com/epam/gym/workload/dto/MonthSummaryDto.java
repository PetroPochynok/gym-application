package com.epam.gym.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthSummaryDto {
    private int month;
    private int trainingSummaryDuration;
}