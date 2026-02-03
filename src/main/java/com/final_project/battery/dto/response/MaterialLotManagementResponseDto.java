package com.final_project.battery.dto.response;

import com.final_project.battery.domain.common.MaterialLotStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class MaterialLotManagementResponseDto {
    private Long id; // 관리용
    private LocalDateTime inboundDate; // 입고일자
    private MaterialLotStatus status; // lot 상태
    private String lotNo; // lot 번호
    private String materialCode; // 자재 코드
    private String materialName; // 자재명
    private BigDecimal currentQty; // 총 재고
    private BigDecimal productionQty; // 생산 투입
    private LocalDateTime statusChangedAt; // 상태변경일시

    public MaterialLotManagementResponseDto(
            Long id,
            LocalDateTime inboundDate,
            MaterialLotStatus status,
            String lotNo,
            String materialCode,
            String materialName,
            BigDecimal currentQty,
            BigDecimal productionQty,
            LocalDateTime statusChangedAt
    ) {
        this.id = id;
        this.inboundDate = inboundDate;
        this.status = status;
        this.lotNo = lotNo;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.currentQty = currentQty;
        this.productionQty = productionQty;
        this.statusChangedAt = statusChangedAt;
    }
}
