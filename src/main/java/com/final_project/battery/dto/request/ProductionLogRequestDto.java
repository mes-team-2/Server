package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductionLogRequestDto {
    private String machineCode;     // 설비 코드
    private String timestamp;       // 생산 시간

    private Integer qty;            // 생산 수량 (보통 1)
    private Boolean isBad;          // 불량 여부

    private String defectType;      // 불량 유형

    // 환경 데이터 스냅샷
    private Double temperature;
    private Double humidity;
    private Double voltage;

    // 작업자 정보
    private String workerCode;

    // 설비가 사용했다고 신고한 자재 Lot ID 목록
    private List<Long> materialLotIds;
}