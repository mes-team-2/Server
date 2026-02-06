package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessProductionStatsDto {
    private String processName; // 공정명
    private Long output;        // 생산량 (Good)
    private Long ng;            // 불량 (Bad)
}