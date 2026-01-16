package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.exception.CustomException;
import com.final_project.battery.exception.ErrorCode;
import com.final_project.battery.repository.*;
import com.final_project.battery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final ProductionLogRepository productionLogRepository;
    private final MachineRepository machineRepository;
    private final LotRepository lotRepository;
    private final ProcessStepRepository processStepRepository;
    private final WorkerRepository workerRepository;
    private final DefectLogRepository defectLogRepository;
    private final InventoryService inventoryService;

    @Transactional
    public void saveProductionLog(ProductionLogRequestDto dto) {
        // 1. 작업자 식별
        String workerCode = SecurityUtil.getCurrentWorkerCode();
        Long workerId = Long.parseLong(workerCode);

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKER_NOT_FOUND));

        // 2. 마스터 데이터 조회
        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new CustomException(ErrorCode.MACHINE_NOT_FOUND));

        Lot lot = lotRepository.findById(dto.getLotId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOT_NOT_FOUND));

        ProcessStep processStep = processStepRepository.findById(dto.getProcessStepId())
                .orElseThrow(() -> new RuntimeException("공정 정보 없음"));

        // 3. 생산 실적(Log) 저장 (모든 공정 공통)
        ProductionLog log = new ProductionLog();
        log.setMachine(machine);
        log.setLot(lot);
        log.setWorkOrder(lot.getWorkOrder());
        log.setProcessStep(processStep);
        log.setWorker(worker);
        log.setGoodQty(dto.getGoodQty());
        log.setBadQty(dto.getBadQty());
        log.setTemperature(dto.getTemperature());
        log.setVoltage(dto.getVoltage());

        productionLogRepository.save(log);

        // 4. 불량 상세 기록 (불량이 있을 경우)
        if (dto.getBadQty() > 0) {
            DefectLog defectLog = new DefectLog();
            defectLog.setProductionLog(log);
            defectLog.setLot(lot);
            defectLog.setMachine(machine);
            defectLog.setWorker(worker);
            defectLog.setDefectType("AUTO_DETECT"); // 자동 감지 시뮬레이터 자동 판정
            defectLog.setDefectQty(dto.getBadQty());
            defectLogRepository.save(defectLog);
        }

        // 5. 공정(검사, SEQ = 5)일 때만 재고 변동
        // 즉 모든 공정이 끝날 시 재고 변동
        if (processStep.getSeq() == 5) {

            // A. 완제품 입고 (양품만)
            if (dto.getGoodQty() > 0) {
                inventoryService.addStockFromProduction(lot, dto.getGoodQty());
            }

            // B. 자재 소모 (양품 + 불량품 전체에 대해 자재 차감)
            int totalUsedQty = dto.getGoodQty() + dto.getBadQty();
            if (totalUsedQty > 0) {
                inventoryService.consumeMaterialForProduction(lot, totalUsedQty);
            }
        }
    }
}
