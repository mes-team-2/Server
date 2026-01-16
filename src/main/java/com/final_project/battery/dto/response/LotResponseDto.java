package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Lot;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LotResponseDto {
    private Long lotId;
    private String lotNo;
    private Integer lotQty;
    private ProductDto product;

    public static LotResponseDto from(Lot lot) {
        return LotResponseDto.builder()
                .lotId(lot.getLotId())
                .lotNo(lot.getLotNo())
                .lotQty(lot.getLotQty())
                .product(ProductDto.builder()
                        .productName(lot.getProduct().getProductName())
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class ProductDto {
        private String productName;
    }
}
