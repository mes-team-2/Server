package com.final_project.battery.dto.response;

import com.final_project.battery.domain.common.MaterialLotStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MaterialLotManagementDetailResponseDto {
    private String materialLotNo; // 자재 lot 번호
    private MaterialLotStatus status; // 자재 lot 상태
    private LocalDateTime txTime; // 최초 입고일
    private String materialCode; // 자재 코드
    private String materialName; // 자재명
    private String unit; // 자재 단위
    private BigDecimal remainQty; // 현 재고
    private List<MaterialLotHistoryDto> histories;  // 투입 이력(리스트)
    private LocalDateTime statusChangedAt; // 최근 상태 변경일
}
