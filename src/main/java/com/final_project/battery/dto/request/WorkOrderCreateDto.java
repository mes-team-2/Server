package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class WorkOrderCreateDto {
    private String productCode;
    private Integer plannedQty;
    private LocalDate dueDate;
}
