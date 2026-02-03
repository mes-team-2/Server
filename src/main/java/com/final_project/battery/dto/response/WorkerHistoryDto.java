package com.final_project.battery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class WorkerHistoryDto {
    private Long id;            // 로그 ID
    private String process;     // 공정명 (설비에서 가져옴)
    private String startTime;
    private String endTime;
    private String defect;      // 불량 여부 (없음 / 전압 미달 등)
}