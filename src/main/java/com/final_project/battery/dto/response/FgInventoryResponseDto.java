package com.final_project.battery.dto.response;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.common.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FgInventoryResponseDto {
    private Integer fgInventoryId;
    private String productName;
    private String lotNo;
    private Integer stockQty;
    private String locationCode;
    private InventoryStatus status;
    private LocalDateTime updatedAt;

    public static FgInventoryResponseDto from(FgInventory entity) {
        return FgInventoryResponseDto.builder()
                .fgInventoryId(entity.getFgInventoryId())
                .productName(entity.getProduct().getProductName())
                .lotNo(entity.getLot().getLotNo())
                .stockQty(entity.getStockQty())
                .locationCode(entity.getLocationCode())
                .status(entity.getStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}