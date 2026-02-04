package com.final_project.battery.service;

import com.final_project.battery.dto.response.DefectLogTableDto;
import com.final_project.battery.repository.DefectLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DefectLogService {

    private final DefectLogRepository defectLogRepository;

    public List<DefectLogTableDto> getDefectLogsForTable(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return defectLogRepository
                .findAll()
                .stream()
                .map(d -> {
                    var pLog = d.getProductionLog();

                    return DefectLogTableDto.builder()
                            .defectLogId(d.getDefectLogId())

                            // LOT
                            .lotNo(
                                    pLog != null && pLog.getLot() != null
                                            ? pLog.getLot().getLotNo()
                                            : "-"
                            )

                            // 작업지시
                            .workOrderNo(
                                    pLog != null
                                            && pLog.getLot() != null
                                            && pLog.getLot().getWorkOrder() != null
                                            ? pLog.getLot().getWorkOrder().getWorkOrderNo()
                                            : "-"
                            )

                            // ✅ 공정 (ProductionLog → ProcessStep)
                            .processCode(
                                    pLog != null && pLog.getProcessStep() != null
                                            ? pLog.getProcessStep().getStepCode()
                                            : "-"
                            )

                            // 설비
                            .machineName(
                                    pLog != null && pLog.getMachine() != null
                                            ? pLog.getMachine().getMachineName()
                                            : "-"
                            )
                            .machineCode(
                                    pLog != null && pLog.getMachine() != null
                                            ? pLog.getMachine().getMachineCode()
                                            : "-"
                            )

                            // 불량 정보
                            .defectType(d.getDefectType().name())
                            .defectQty(d.getDefectQty())

                            .occurredAt(d.getCreatedAt())
                            .build();
                })
                .toList();
    }
}
