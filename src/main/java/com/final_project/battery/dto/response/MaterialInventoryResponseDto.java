package com.final_project.battery.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MaterialInventoryResponseDto {
    private Long no;             // key: "no" (materialId 매핑)
    private String materialCode; // key: "materialCode"
    private String materialName; // key: "materialName"
    private BigDecimal stockQty; // key: "stockQty"
    private Integer safeQty;     // key: "safeQty" (추가됨)
    private String unit;         // key: "unit"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; // key: "createdAt"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime inboundAt; // key: "inboundAt" (최근 입고일/수정일)

    public String getStockStatus() {
        if (stockQty == null) return "DANGER";

        int safe = (safeQty != null) ? safeQty : 0;

        if (stockQty.compareTo(BigDecimal.valueOf(safe)) >= 0) {
            return "SAFE";
        } else if (stockQty.compareTo(BigDecimal.ZERO) == 0) {
            return "DANGER";
        } else {
            return "CAUTION";
        }
    }

    public MaterialInventoryResponseDto(Long no, String materialCode, String materialName,
                                        BigDecimal stockQty, String unit, Integer safeQty,
                                        LocalDateTime createdAt, LocalDateTime inboundAt) {
        this.no = no;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.stockQty = (stockQty != null) ? stockQty : BigDecimal.ZERO;
        this.unit = unit;
        this.safeQty = (safeQty != null) ? safeQty : 1000;
        this.createdAt = createdAt;
        this.inboundAt = inboundAt;
    }
}