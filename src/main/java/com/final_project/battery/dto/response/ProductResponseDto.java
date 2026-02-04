package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponseDto {
    private Long productId;
    private String productCode;
    private String productName;
    private Integer voltage;
    private Integer capacityAh;
    private String unit;
    private String type;
    private Boolean active;
    private String updatedAt;
    private String createdAt;

    public ProductResponseDto(Product p) {
        this.productId = p.getProductId();
        this.productCode = p.getProductCode();
        this.productName = p.getProductName();
        this.voltage = p.getVoltage();
        this.capacityAh = p.getCapacityAh();
        this.unit = p.getUnit();
        this.type = "완제품";
        this.active = true;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (p.getUpdatedAt() != null) {
            this.updatedAt = p.getUpdatedAt().format(formatter);
        } else {
            this.updatedAt = "-";
        }

        // 등록일 매핑 추가
        if (p.getCreatedAt() != null) {
            this.createdAt = p.getCreatedAt().format(formatter);
        } else {
            this.createdAt = "-";
        }
    }
}