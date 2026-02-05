package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Lot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotResponseDto {
    private Long id;
    private String lotNo;       // LOT 번호
    private String productName; // 제품명
    private String workOrderNo; // 작업지시번호
    private Integer currentQty; // 현재수량
    private Integer badQty;     // 불량 (5공정 기준)
    private String createdAt;   // 생성일

    // [로직용 필드] 화면 컬럼엔 없지만, 수량 표시 로직('-')을 위해 필요
    private String status;

    public static ProductLotResponseDto of(Lot lot, int currentQty, int defectQty) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return ProductLotResponseDto.builder()
                .id(lot.getLotId())
                .lotNo(lot.getLotNo())
                .productName(lot.getProduct().getProductName())
                .workOrderNo(lot.getWorkOrder().getWorkOrderNo())
                .currentQty(currentQty)
                .badQty(defectQty)
                .createdAt(lot.getCreatedAt().format(fmt))
                .status(lot.getStatus().name()) // 로직 처리용 데이터
                .build();
    }
}