package com.final_project.battery.dto.response;

import com.final_project.battery.domain.ProcessLog;
import com.final_project.battery.domain.ProductionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessLogResponseDto {
    private Long id;
    private String lotNo;       // LOT 번호
    private String processStep; // 공정명
    private String machineName; // 설비명
    private String workerName;  // 작업자명
    private String status;      // PASS / FAIL
    private String startTime;
    private String endTime;

    // 상세 정보 (ProductionLog에서 가져옴)
    private Integer goodQty;    // 양품 수량
    private Integer badQty;     // 불량 수량
    private Double temperature;
    private Double humidity;
    private Double voltage;

    public static ProcessLogResponseDto from(ProcessLog pl, ProductionLog prod) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return ProcessLogResponseDto.builder()
                .id(pl.getProcessLogId())
                .lotNo(pl.getLot().getLotNo())
                .processStep(pl.getProcessStep().getStepName())
                .machineName(pl.getMachine().getMachineName())
                .workerName(pl.getWorker() != null ? pl.getWorker().getWorkerName() : "-")
                .status(pl.getStatus().name())
                .startTime(pl.getStartTime() != null ? pl.getStartTime().format(fmt) : "-")
                .endTime(pl.getEndTime() != null ? pl.getEndTime().format(fmt) : "-")
                // ProductionLog가 있으면 상세 정보 매핑
                .goodQty(prod != null ? prod.getGoodQty() : 0)
                .badQty(prod != null ? prod.getBadQty() : 0)
                .temperature(prod != null ? prod.getTemperature() : 0.0)
                .humidity(prod != null ? prod.getHumidity() : 0.0)
                .voltage(prod != null ? prod.getVoltage() : 0.0)
                .build();
    }
}