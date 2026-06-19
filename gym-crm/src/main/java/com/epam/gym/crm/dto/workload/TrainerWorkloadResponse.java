package com.epam.gym.crm.dto.workload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkloadResponse {
    private String username;
    private String firstName;
    private String lastName;

    @JsonProperty("isActive")
    private boolean isActive;

    private List<YearSummaryDto> years;
}