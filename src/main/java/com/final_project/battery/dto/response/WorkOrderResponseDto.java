package com.final_project.battery.dto.response;

import com.final_project.battery.domain.WorkOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class WorkOrderResponseDto {
    private String id;          // 작업지시 번호
    private String workOrderNo;
    private String product;     // 제품명
    private Integer planQty;    // 계획 수량
    private String status;      // 상태 (WAIT, IN_PROGRESS, DONE)
    private String manager;
    private String startDate;   // 시작 시간 (yyyy-MM-dd HH:mm)
    private String endDate;     // 종료 시간 (yyyy-MM-dd HH:mm)
    private String dueDate;     // 납기일 (yyyy-MM-dd)

    // Entity -> DTO
    public WorkOrderResponseDto(WorkOrder wo) {
        this.id = wo.getWorkOrderNo();
        this.product = wo.getProduct().getProductName();
        this.planQty = wo.getPlannedQty();
        this.status = wo.getStatus().name();

        DateTimeFormatter datetimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        this.startDate = wo.getStartedAt() != null ? wo.getStartedAt().format(datetimeFmt) : "-";
        this.endDate = wo.getEndedAt() != null ? wo.getEndedAt().format(datetimeFmt) : "-";
        this.dueDate = wo.getDueDate() != null ? wo.getDueDate().format(dateFmt) : "-";

        this.manager = (wo.getManager() != null) ? wo.getManager().getWorkerName() : "관리자";
    }
}