package com.final_project.battery.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final_project.battery.domain.MaterialLot;
import com.final_project.battery.domain.common.MaterialLotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialLotResponseDto {
    private Long materialLotId;
    private String materialLotNo; // Lot 번호 (ML-MAT-001-INIT)
    private BigDecimal inQty;     // 최초 입고량
    private BigDecimal remainQty; // 현재 잔량
    private MaterialLotStatus status; // AVAILABLE, EXHAUSTED

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime inputDate; // 입고일

    // Entity -> DTO 변환 메서드
    public static MaterialLotResponseDto from(MaterialLot entity) {
        return MaterialLotResponseDto.builder()
                .materialLotId(entity.getMaterialLotId())
                .materialLotNo(entity.getMaterialLotNo())
                .inQty(entity.getInQty())
                .remainQty(entity.getRemainQty())
                .status(entity.getStatus())
                .inputDate(entity.getInputDate())
                .build();
    }
}