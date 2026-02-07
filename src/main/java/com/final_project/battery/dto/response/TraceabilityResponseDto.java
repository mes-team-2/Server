package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TraceabilityResponseDto {
    private String lot; // 생산 로트 번호
    private String productName; // 제품명
    private LocalDateTime testedAt; // 생산일
    private Long totalQty; // 총 수량
    private Long goodQty; // 양품 수
    private Long badQty; // 불량 수
    private Double yieldRate; // 수율

    // 기본 lot로 조회
    // 총 수량은 quilty_test 컬럼의 해당 로트의 수 count
    // 양품 수는 quility_Test 컬럼의 ok 수
    // 불량 수는 quility_Test 컬럼의 ng 수
    // 수율
}
