package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FgInventoryManagementResponseDto {
    private Integer id; // id
    private String productCode; // 제품 코드
    private String productName; // 제품명
    private String unit; // 단위
    private Integer stockQty; // 현재재고
    private LocalDateTime updatedAt;// 최종입고일
}
