package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionLogRequestDto {
    private Long machineId;      // 설비 ID
    private Long lotId;          // Lot ID
    private Long processStepId;  // 공정 단계 ID

    private Integer goodQty;     // 양품
    private Integer badQty;      // 불량

    private Double temperature;  // 온도
    private Double voltage;      // 전압
}