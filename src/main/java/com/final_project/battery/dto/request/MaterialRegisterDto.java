package com.final_project.battery.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class MaterialRegisterDto {
    @NotBlank(message = "자재명은 필수입니다.")
    private String materialName;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit; // KG, L, EA, M

    @Min(value = 0, message = "안전재고는 0 이상이어야 합니다.")
    private Integer safeQty;

    private BigDecimal initialStock;
}
