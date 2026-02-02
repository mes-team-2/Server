package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessStepCreateDto {
    private Integer seq;
    private String processCode;
    private String processName;
    private Boolean active;
}