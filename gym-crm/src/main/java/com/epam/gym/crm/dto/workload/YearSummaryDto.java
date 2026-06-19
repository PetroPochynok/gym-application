package com.epam.gym.crm.dto.workload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearSummaryDto {
    private int year;
    private List<MonthSummaryDto> months;
}