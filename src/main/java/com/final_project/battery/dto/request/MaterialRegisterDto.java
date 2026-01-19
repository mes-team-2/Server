package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class MaterialRegisterDto {
    private String materialName;
    private String unit;
    private BigDecimal initialStock; // 기초 재고
}
