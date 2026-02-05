package com.final_project.battery.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ProductLotDetailDto {
    // 1. LOT 정보
    private Long lotId;
    private String lotNo;
    private String status;
    private Integer currentQty;
    private String createdAt;

    // 2. 작업지시 정보
    private String workOrderNo;
    private String manager;
    private String workOrderCreatedAt;
    private String productName;
    private String workOrderStatus;
    private Integer plannedQty;
    private String workOrderStartDate;
    private String workOrderDueDate;

    // 3. 리스트 데이터
    private List<ProcessProgressDto> processList;
    private List<MaterialInputDto> materialList;

    @Getter @Builder
    public static class ProcessProgressDto {
        private Long id;
        private String stepName;    // 공정명
        private String machineName; // 설비명
        private String status;      // 상태
        private String startedAt;   // 시작일시 (yyyy-MM-dd HH:mm)
        private String endedAt;     // 종료일시 (yyyy-MM-dd HH:mm)
    }

    @Getter @Builder
    public static class MaterialInputDto {
        private Long id;
        // materialCode 삭제 (요청 반영)
        private String lotNo;       // 자재 LOT
        private String materialName;// 자재명
        private String qty;         // 총 투입 수량
        private String unit;        // 단위
        private String time;        // 최초 투입 일시
    }
}