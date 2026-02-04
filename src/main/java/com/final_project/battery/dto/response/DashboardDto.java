package com.final_project.battery.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DashboardDto {
    private String timestamp; // 서버 기준 현재 시간
    private SummaryDto summary; // 상단 요약 (달성률, 양품, 불량)
    private List<ProductionTrendDto> productionTrend; // 시간별 생산 현황
    private List<DefectAnalysisDto> defectAnalysis; // 불량 유형 분석
    private List<MachineStatusDto> machineStatus; // 설비 가동 현황
    private WorkerInfoDto workerInfo; // 작업자 현황
    private ProcessEffDto processEff; // 공정 효율 (OEE)

    @Getter @Setter @Builder
    public static class SummaryDto {
        private Double achievementRate; // 달성률
        private Integer totalActual;    // 양품 수량
        private Double actualRate;      // 양품률
        private Integer totalDefect;    // 불량 수량
        private Double defectRate;      // 불량률
    }

    @Getter @Setter @Builder
    public static class ProductionTrendDto {
        private String time;   // "09:00"
        private Integer actual; // 양품
        private Integer defect; // 불량
        private Double defectRate;
    }

    @Getter @Setter @Builder
    public static class DefectAnalysisDto {
        private String name; // 불량 유형 (한글)
        private Long value;  // 개수
    }

    @Getter @Setter @Builder
    public static class MachineStatusDto {
        private String machineCode;
        private String machineName;
        private Double temperature;
        private Double humidity;
        private Double voltage;
        private String status; // RUN, STOP...
    }

    @Getter @Setter @Builder
    public static class WorkerInfoDto {
        private Long total;
        private Long working;
        private Long standby;
        // 팀 정보는 백엔드에 없으므로 프론트 하드코딩 유지하거나 임시 데이터 전송
    }

    @Getter @Setter @Builder
    public static class ProcessEffDto {
        private Double oee;
        private Double availability;
        private Double performance;
        private Double defectRate;
        private Double materialUsage; // 자재 소모율
    }
}