package com.final_project.battery.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProductReportResponse {

    private LocalDate date;        // 일자
    private String productName;    // 제품명

    private Long planQty;          // 계획수량
    private Long totalAttemptQty;  // 실제 시도수
    private Long goodQty;          // 양품
    private Long badQty;           // 불량

    private Double yieldRate;      // 수율
    private Double defectRate;     // 불량률

    public ProductReportResponse(
            LocalDate date,
            String productName,
            Long planQty,
            Long totalAttemptQty,
            Long goodQty,
            Long badQty,
            Double yieldRate,
            Double defectRate
    ) {
        this.date = date;
        this.productName = productName;
        this.planQty = planQty;
        this.totalAttemptQty = totalAttemptQty;
        this.goodQty = goodQty;
        this.badQty = badQty;
        this.yieldRate = yieldRate;
        this.defectRate = defectRate;
    }
}
