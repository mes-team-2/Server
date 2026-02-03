package com.final_project.battery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class WorkerDetailDto {
    private WorkerResponseDto workerInfo; // 기본 정보
    private String currentStatus;         // 현재 상태 (근무중, 대기, 퇴근)
    private List<WorkerHistoryDto> history; // 최근 이력 리스트
}