package com.final_project.battery.dto.response;

import java.time.LocalDateTime;

public class TraceabilityResponseDto {
    private String lot; // 생산 로트 번호
    private String productName; // 제품명
    private LocalDateTime endedAt; // 생산일
    private Long planQty; // 계획 수량
    private Long totalQty; // 총 수량
    private Long goodQty; // 양품 수
    private Long badQty; // 불량 수
    private Double yield; // 수율
}
