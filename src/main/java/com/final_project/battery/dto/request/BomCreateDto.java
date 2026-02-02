package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BomCreateDto {
    private String productCode;
    private String materialCode;
    private Double qty;
    private String process;
}