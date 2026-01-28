package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @ToString
public class StatusLogRequestDto {
    private String machineCode; // 설비 코드
    private String workerCode;  // 상태를 변경한 작업자 (또는 SYSTEM)
    private String status;      // RUN, STOP, WAIT, DOWN
    private String reasonCode;  // 고장 시 사유 코드 (옵션)
    private String timestamp;   // 발생 시간
}