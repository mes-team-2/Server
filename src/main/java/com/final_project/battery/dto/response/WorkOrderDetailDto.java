package com.final_project.battery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WorkOrderDetailDto {
    // 1. 기본 정보 (WorkOrder)
    private String workOrderNo;
    private String productName;
    private Integer plannedQty;
    private String status;

    // 2. Lot 정보
    private LotInfoDto lotInfo;

    // 3. 공정 진행 리스트
    private List<ProcessProgressDto> processList;

    // 4. 자재 투입 리스트
    private List<MaterialInputDto> materialList;

    @Getter @Setter @NoArgsConstructor
    public static class LotInfoDto {
        private String lotNo;
        private Integer qty;
        private String status;
        private String createdAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class ProcessProgressDto {
        private Long id;
        private String stepName;
        private String machineName;
        private String status;
        private String startedAt;
        private String endedAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class MaterialInputDto {
        private Long id;
        private String materialName;
        private String qty; // 소수점 표현을 위해 String 권장
        private String unit;
        private String time;
    }
}