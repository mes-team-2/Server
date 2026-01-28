package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SensorLogRequestDto {
    private String machineCode;
    private String timestamp;

    private EnvData data;

    @Getter @Setter @NoArgsConstructor
    public static class EnvData {
        private Double temperature;
        private Double humidity;
        private Double voltage;
    }
}
