package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerCreateDto {
    private String workerNo;
    private String name;
    private String position; // "작업자", "관리자" 등 한글로 들어옴
    private Boolean active;
}