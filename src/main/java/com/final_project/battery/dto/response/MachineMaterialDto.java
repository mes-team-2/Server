package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineMaterialDto {
    private String machineCode; // 설비 코드
    private Long materialLotId;     // 자재 LOT ID
    private String materialName;    // 자재명
    private String materialCode;    // 자재코드
    private Double remainQty;       // 잔량
}
