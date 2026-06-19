package com.epam.gym.crm.dto.workload;

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