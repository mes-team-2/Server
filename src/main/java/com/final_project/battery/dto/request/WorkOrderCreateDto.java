package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkOrderCreateDto {
    private String productCode;
    private Integer plannedQty;
    private String dueDate;
}