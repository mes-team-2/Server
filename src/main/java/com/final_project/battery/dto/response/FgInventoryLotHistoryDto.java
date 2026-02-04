package com.final_project.battery.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FgInventoryLotHistoryDto {
    private String lot; // lot 번호
    private Integer qty; // 재고
    private LocalDateTime time; // 생산일
    private String order; // 작업지시
    private String workerName; // 담당자
}
