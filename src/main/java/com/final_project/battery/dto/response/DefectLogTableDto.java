package com.final_project.battery.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DefectLogTableDto {

    private Integer defectLogId;

    private String lotNo;
    private String workOrderNo;

    private String processCode;
    private String machineCode;
    private String machineName;

    private String defectType;   // VOLTAGE
    private int defectQty;

    private LocalDateTime occurredAt;
}