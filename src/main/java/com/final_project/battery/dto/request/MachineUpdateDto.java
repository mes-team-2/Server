package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MachineUpdateDto {
    private String machineName;
    private String processCode;
    private Boolean active;
}