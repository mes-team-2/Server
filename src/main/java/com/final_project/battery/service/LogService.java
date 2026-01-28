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
    private final ProcessStepRepository processStepRepository; // [추가]
    private final QualityTestRepository qualityTestRepository; // [추가] 품질검사 저장용

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
        // (1) 설비 및 작업자 조회
        Machine machine = machineRepository.findByMachineCode(dto.getMachineCode())
                .orElseThrow(() -> new RuntimeException("미등록 설비"));

        Worker worker = workerRepository.findByWorkerCode(dto.getWorkerCode())
                .orElse(null);

        // (2) 작업지시 찾기
        WorkOrder workOrder = workOrderRepository.findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus.IN_PROGRESS)
                .orElseThrow(() -> new RuntimeException("진행 중인 작업지시 없음"));

        // (3) [핵심 수정] Lot를 새로 만들지 않고, 작업지시에 연결된 기존 Lot를 찾음
        // DataInitializer에서 작업지시 생성 시 Lot도 같이 만들어뒀기 때문에 반드시 존재해야 함.
        Lot productLot = lotRepository.findFirstByWorkOrder(workOrder)
                .orElseGet(() -> {
                    // 만약 없으면 비상용으로 하나 생성 (안전장치)
                    String lotNo = "LOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-EMERGENCY";
                    Lot newLot = Lot.builder()
                            .lotNo(lotNo)
                            .product(workOrder.getProduct())
                            .workOrder(workOrder)
                            .lotQty(workOrder.getPlannedQty())
                            .status(LotStatus.IN_PROGRESS)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return lotRepository.save(newLot);
                });

        // (4) 자재 차감 (기존과 동일)
        StringBuilder materialSnapshot = new StringBuilder();
        if (dto.getMaterialLotIds() != null) {
            for (Long matLotId : dto.getMaterialLotIds()) {
                processMaterialConsumption(matLotId, dto.getQty(), workOrder, productLot, materialSnapshot);
            }
        }

        // (5) ProductionLog 저장 (개별 생산 이력은 여기에 남김)
        ProductionLog prodLog = new ProductionLog();
        prodLog.setMachine(machine);
        prodLog.setWorkOrder(workOrder);
        prodLog.setLot(productLot);   // 메인 Lot에 연결
        prodLog.setWorker(worker);
        prodLog.setGoodQty(dto.getIsBad() ? 0 : dto.getQty());
        prodLog.setBadQty(dto.getIsBad() ? dto.getQty() : 0);
        prodLog.setStartedAt(LocalDateTime.parse(dto.getTimestamp()).minusSeconds(2)); // 시뮬레이션 시간 보정
        prodLog.setEndedAt(LocalDateTime.parse(dto.getTimestamp()));
        prodLog.setUsedMaterialInfo(materialSnapshot.toString());
        prodLog.setTemperature(dto.getTemperature());
        prodLog.setHumidity(dto.getHumidity());
        prodLog.setVoltage(dto.getVoltage());

        productionLogRepository.save(prodLog);

        // (6) ProcessLog 저장 (공정 이력)
        ProcessLog processLog = new ProcessLog();
        processLog.setLot(productLot); // 메인 Lot에 연결

        // 설비 코드로 공정 단계 찾기 (ProcessStepRepository 필요)
        // 만약 processStepRepository가 없다면 machine.getProcessCode() 문자열이라도 사용
        ProcessStep step = processStepRepository.findByStepCode(machine.getProcessCode())
                .orElse(null);

        processLog.setProcessStep(step);
        processLog.setMachine(machine);
        processLog.setWorker(worker);
        processLog.setStartTime(prodLog.getStartedAt());
        processLog.setEndTime(prodLog.getEndedAt());
        processLog.setStatus(dto.getIsBad() ? ProcessLogStatus.FAIL : ProcessLogStatus.PASS);
        processLogRepository.save(processLog);

        // (7) 품질 검사(QualityTest) 이력 저장 (검사 공정인 경우 or 불량 발생 시)
        // 여기서는 모든 생산 건에 대해 간단히 저장하거나, 특정 공정만 저장할 수 있음
        if (machine.getProcessCode().contains("PROC-50") || dto.getIsBad()) {
            QualityTest qualityTest = new QualityTest();
            qualityTest.setLot(productLot);
            qualityTest.setMachine(machine);
            qualityTest.setWorker(worker);
            qualityTest.setResult(dto.getIsBad() ? QualityTestResult.FAIL : QualityTestResult.PASS);
            qualityTest.setTestedAt(LocalDateTime.now());
            qualityTestRepository.save(qualityTest);
        }

        // (8) 불량 발생 시 DefectLog 저장
        if (dto.getIsBad()) {
            saveDefectLog(dto, prodLog, machine, worker, productLot);
        }

        log.info("✅ 생산 실적 기록 [Lot:{}] [설비:{}] [결과:{}]", productLot.getLotNo(), machine.getMachineCode(), dto.getIsBad() ? "불량" : "양품");
    }

    private void processMaterialConsumption(Long matLotId, Integer prodQty, WorkOrder wo, Lot productLot, StringBuilder snapshot) {
        MaterialLot matLot = materialLotRepository.findById(matLotId)
                .orElseThrow(() -> new RuntimeException("자재 Lot 없음: " + matLotId));

        BigDecimal requiredPerUnit = BigDecimal.ONE;
        if (wo != null) {
            Optional<BOM> bomOpt = bomRepository.findByProductAndMaterial(wo.getProduct(), matLot.getMaterial());
            if (bomOpt.isPresent()) requiredPerUnit = bomOpt.get().getRequiredQty();
        }

        BigDecimal consumeQty = requiredPerUnit.multiply(BigDecimal.valueOf(prodQty));
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
        snapshot.append(matLot.getMaterial().getMaterialName()).append("(").append(consumeQty).append(")");
    }

    private void saveDefectLog(ProductionLogRequestDto dto, ProductionLog prodLog, Machine machine, Worker worker, Lot lot) {
        DefectLog defect = new DefectLog();
        defect.setProductionLog(prodLog);
        defect.setMachine(machine);
        defect.setWorker(worker);
        defect.setLot(lot);
        defect.setDefectQty(dto.getQty());

        try {
            defect.setDefectType(DefectType.valueOf(dto.getDefectType()));
        } catch (Exception e) {
            defect.setDefectType(DefectType.ETC);
        }

        defect.setTemperature(dto.getTemperature());
        defect.setHumidity(dto.getHumidity());
        defect.setVoltage(dto.getVoltage());
        defect.setCreatedAt(LocalDateTime.now());

        defectLogRepository.save(defect);
    }
}