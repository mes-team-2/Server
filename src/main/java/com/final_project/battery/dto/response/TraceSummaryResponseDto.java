package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TraceSummaryResponseDto {
    private Long countLot; // 조회된 lot 수
    private Long allQty; // 전체 수량
    private Long allGoodQty; // 총 양품수
    private Long allBadQty; // 총 불량 수
}
