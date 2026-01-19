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
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal stockQty; // 현재 재고량
    private String unit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public String getInventoryStatus() {
        // 재고 정보가 아예 없으면 DANGER
        if (stockQty == null) return "DANGER";

        // 1. SAFE (안전): 5000개 이상
        if (stockQty.compareTo(new BigDecimal("5000")) >= 0) {
            return "SAFE";
        }
        // 2. WARNING (주의): 2000개 이상 ~ 5000개 미만
        else if (stockQty.compareTo(new BigDecimal("2000")) >= 0) {
            return "WARNING";
        }
        // 3. DANGER (위험): 2000개 미만
        else {
            return "DANGER";
        }
    }
}
