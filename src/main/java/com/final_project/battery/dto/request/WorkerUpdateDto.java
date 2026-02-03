package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerUpdateDto {
    private String name;
    private String position;
    private Boolean active;
}