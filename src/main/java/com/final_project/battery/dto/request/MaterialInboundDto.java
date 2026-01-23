package com.final_project.battery.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class MaterialInboundDto {

    @NotNull(message = "자재 ID는 필수입니다.")
    private Long materialId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "입고 수량은 0보다 커야 합니다.")
    private BigDecimal quantity;
}