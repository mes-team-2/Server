package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.*;
import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.dto.request.SensorLogRequestDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final MachineRepository machineRepository;
    private final SensorLogRepository sensorLogRepository;
    private final ProductionLogRepository productionLogRepository;
    private final DefectLogRepository defectLogRepository;
    private final MaterialLotRepository materialLotRepository;
    private final MaterialTxRepository materialTxRepository;
    private final BomRepository bomRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ProcessLogRepository processLogRepository;
    private final LotRepository lotRepository;
    private final WorkerRepository workerRepository;
    private final ProcessStepRepository processStepRepository;
    private final QualityTestRepository qualityTestRepository;
    private final MachineStatusLogRepository machineStatusLogRepository;

    // 1. 센서 로그 저장
    @Transactional
    public void saveSensorLog(SensorLogRequestDto dto) {
        Machine machine = machineRepository.findByMachineCode(dto.getMachineCode())
                .orElseThrow(() -> new RuntimeException("미등록 설비: " + dto.getMachineCode()));

        sensorLogRepository.save(SensorLog.builder()
                .machine(machine)
                .temperature(dto.getData().getTemperature())
                .humidity(dto.getData().getHumidity())
                .voltage(dto.getData().getVoltage())
                .recordedAt(LocalDateTime.parse(dto.getTimestamp()))
                .build());
    }

    // 2. 생산 실적 처리
    @Transactional
    public void saveProductionLog(ProductionLogRequestDto dto) {
        Machine machine = machineRepository.findByMachineCode(dto.getMachineCode())
                .orElseThrow(() -> new RuntimeException("미등록 설비"));

        Worker worker = workerRepository.findByWorkerCode(dto.getWorkerCode()).orElse(null);

        // (1) 현재 작업지시 찾기
        WorkOrder workOrder = workOrderRepository.findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus.IN_PROGRESS)
                .orElseThrow(() -> new RuntimeException("진행 중인 작업지시 없음"));

        // (2) Lot 찾기 (새로 만들지 않고 기존 Lot 사용)
        Lot productLot = lotRepository.findFirstByWorkOrder(workOrder)
                .orElseThrow(() -> new RuntimeException("작업지시에 해당하는 Lot가 없습니다."));

        // (3) 자재 차감
        StringBuilder materialSnapshot = new StringBuilder();
        if (dto.getMaterialLotIds() != null) {
            for (Long matLotId : dto.getMaterialLotIds()) {
                processMaterialConsumption(matLotId, dto.getQty(), workOrder, productLot, materialSnapshot);
            }
        }

        // (4) 설비 코드로 공정 단계(ProcessStep) 찾기 [NULL 해결 핵심]
        ProcessStep step = processStepRepository.findByStepCode(machine.getProcessCode())
                .orElseThrow(() -> new RuntimeException("공정 정보 없음: " + machine.getProcessCode()));

        // (5) ProductionLog 저장
        ProductionLog prodLog = new ProductionLog();
        prodLog.setMachine(machine);
        prodLog.setWorkOrder(workOrder);
        prodLog.setLot(productLot);
        prodLog.setProcessStep(step);
        prodLog.setWorker(worker);
        prodLog.setGoodQty(dto.getIsBad() ? 0 : dto.getQty());
        prodLog.setBadQty(dto.getIsBad() ? dto.getQty() : 0);
        prodLog.setTemperature(dto.getTemperature());
        prodLog.setHumidity(dto.getHumidity());
        prodLog.setVoltage(dto.getVoltage());
        prodLog.setUsedMaterialInfo(materialSnapshot.toString());
        prodLog.setStartedAt(LocalDateTime.parse(dto.getTimestamp()).minusSeconds(2));
        prodLog.setEndedAt(LocalDateTime.parse(dto.getTimestamp()));
        productionLogRepository.save(prodLog);

        ProcessLog processLog = new ProcessLog();
        processLog.setLot(productLot);
        processLog.setProcessStep(step);
        processLog.setMachine(machine);
        processLog.setWorker(worker);
        processLog.setStartTime(prodLog.getStartedAt());
        processLog.setEndTime(prodLog.getEndedAt());
        processLog.setStatus(dto.getIsBad() ? ProcessLogStatus.FAIL : ProcessLogStatus.PASS);
        processLogRepository.save(processLog);

        if (dto.getIsBad()) {
            saveDefectLog(dto, prodLog, machine, worker, productLot);
        }

        log.info("✅ 생산 기록: [Lot:{}] [설비:{}] [공정:{}] [Temp:{}C]",
                productLot.getLotNo(), machine.getMachineCode(), step.getStepName(), dto.getTemperature());
    }

    @Transactional
    public void saveMachineStatusLog(Map<String, String> body) {
        String machineCode = body.get("machineCode");
        String newStatus = body.get("status"); // RUN, STOP, WAIT, DOWN
        String reason = body.getOrDefault("reason", "");
        String workerCode = body.get("workerCode");

        Machine machine = machineRepository.findByMachineCode(machineCode)
                .orElseThrow(() -> new RuntimeException("설비 없음"));

        // 상태가 이전과 동일하면 로그를 쌓지 않음 (선택 사항, 여기선 무조건 남기거나 비즈니스 로직에 따름)
        // 하지만 시뮬레이터가 '변경될 때만' 보내기로 약속하면 그냥 저장하면 됩니다.

        // 1. Machine 마스터 정보 업데이트 (현재 상태 갱신)
        try {
            machine.setStatus(MachineStatus.valueOf(newStatus));
        } catch (Exception e) {
            log.warn("알 수 없는 상태 코드: {}", newStatus);
        }

        // 2. 로그 저장
        MachineStatusLog logEntity = new MachineStatusLog();
        logEntity.setMachine(machine);

        // 작업자 정보가 있으면 넣고, 없으면 SYSTEM 처리
        if (workerCode != null && !workerCode.equals("UNKNOWN")) {
            workerRepository.findByWorkerCode(workerCode).ifPresent(logEntity::setWorker);
        }

        logEntity.setStatus(newStatus);
        logEntity.setReasonCode(reason);
        logEntity.setStartTime(LocalDateTime.now());
        // endTime은 다음 상태가 들어올 때 업데이트하는 방식이 정석이나,
        // 단순 로그형(이벤트형) 기록이라면 startTime만 있어도 분석 가능합니다.

        machineStatusLogRepository.save(logEntity);

        log.info("🚦 설비 상태 변경: [{}] {} -> {} ({})",
                machineCode, machine.getStatus(), newStatus, reason);
    }

    private void processMaterialConsumption(Long matLotId, Integer prodQty, WorkOrder wo, Lot productLot, StringBuilder snapshot) {
        MaterialLot matLot = materialLotRepository.findById(matLotId)
                .orElseThrow(() -> new RuntimeException("자재 Lot 없음"));

        // 1. 해당 제품(Product)과 자재(Material)에 맞는 BOM 정보 조회
        BOM bom = bomRepository.findByProductAndMaterial(wo.getProduct(), matLot.getMaterial())
                .orElse(null);

        // 2. BOM이 있으면 그 수량만큼, 없으면 0 (혹은 1) 처리
        BigDecimal requiredPerUnit = (bom != null) ? bom.getRequiredQty() : BigDecimal.ZERO;

        // 3. 생산수량 * 단위소모량 = 총 소모량
        BigDecimal consumeQty = requiredPerUnit.multiply(BigDecimal.valueOf(prodQty));

        // 4. 재고 차감 (0보다 클 때만)
        if (consumeQty.compareTo(BigDecimal.ZERO) > 0) {
            matLot.setRemainQty(matLot.getRemainQty().subtract(consumeQty));

            MaterialTx tx = MaterialTx.builder()
                    .material(matLot.getMaterial())
                    .materialLot(matLot)
                    .workOrder(wo)
                    .lot(productLot)
                    .qty(consumeQty)
                    .txType(TxType.CONSUME)
                    .txTime(LocalDateTime.now())
                    .build();
            materialTxRepository.save(tx);

            if (snapshot.length() > 0) snapshot.append(", ");
            snapshot.append(matLot.getMaterial().getMaterialName())
                    .append("(").append(consumeQty).append(matLot.getMaterial().getUnit()).append(")");
        }
    }

    private void saveDefectLog(ProductionLogRequestDto dto, ProductionLog prodLog, Machine machine, Worker worker, Lot lot) {
        DefectLog defect = new DefectLog();
        defect.setProductionLog(prodLog);
        defect.setMachine(machine);
        defect.setWorker(worker);
        defect.setLot(lot);
        defect.setDefectQty(dto.getQty());
        try { defect.setDefectType(DefectType.valueOf(dto.getDefectType())); } catch (Exception e) { defect.setDefectType(DefectType.ETC); }
        defect.setTemperature(dto.getTemperature());
        defect.setHumidity(dto.getHumidity());
        defect.setVoltage(dto.getVoltage());
        defect.setCreatedAt(LocalDateTime.now());
        defectLogRepository.save(defect);
    }
}