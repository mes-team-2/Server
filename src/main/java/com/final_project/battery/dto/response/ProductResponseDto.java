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

    public ProductResponseDto(Product p) {
        this.productId = p.getProductId();
        this.productCode = p.getProductCode();
        this.productName = p.getProductName();
        this.voltage = p.getVoltage();
        this.capacityAh = p.getCapacityAh();
        this.unit = p.getUnit();
        this.type = "완제품";
        this.active = true;

        if (p.getUpdatedAt() != null) {
            this.updatedAt = p.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            this.updatedAt = "-";
        }
    }
}