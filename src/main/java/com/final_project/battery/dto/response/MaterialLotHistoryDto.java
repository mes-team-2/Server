package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MaterialLotHistoryDto {
    private LocalDateTime inputDate; // 투입일
    private String Lot; // lot 번호
    private BigDecimal qty; // 투입 수량
}
