package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionReportResponseDto {
    private String date;        // 일자 (YYYY-MM-DD)
    private String productName; // 제품명
    private Long plan;          // 계획
    private Long prod;          // 실적 (양품 + 불량)
    private Long ok;            // 양품
    private Long ng;            // 불량
    private Double yield;       // 수율
    private Double defectRate;  // 불량률
}