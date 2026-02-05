package com.final_project.battery.dto.response;


import com.final_project.battery.domain.common.DefectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TestLogResponseDto {
    private String productName; // 제품명(상세용)
    private LocalDateTime endedAt; // 검사일시(endtime 기준으로함)
    private Boolean isOk; // badQty를 통한 판정 1과 0으로 구분, 1일 때 ng, 0일 때 ok
    private DefectType defectType; // 불량코드(유형) ok일 경우 null
    private String lotNo; // 생산 LOT
    private String workOrderNo; // 작업 지시
    private String processStepName; // 공정명
    private String machineName; // 설비명
    private Double temperature; // 온도
    private Double humidity; // 습도
    private Double voltage; // 전압
    private String workerCode; // 검사자

    // productionLog, defectLog, processStep, workorder, worker, lot, machine join으로
}
