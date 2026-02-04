package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@Setter
public class FgInventoryManagementDetailResponseDto {
    private Integer id; // 관리용
    private String productCode; // 제품 코드
    private String productName; // 제품명
    private Integer qty; // 총 재고
    private List<FgInventoryLotHistoryDto> histories; // 해당 제품 코드 가진 로트 리스트
}
