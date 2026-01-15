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

    // [중요] 재고 서비스 주입
    private final InventoryService inventoryService;

    @Transactional
    public void saveProductionLog(ProductionLogRequestDto dto) {
        // 1. 현재 작업자 식별
        String workerCode = SecurityUtil.getCurrentWorkerCode();
        Worker worker = workerRepository.findByWorkerCode(workerCode)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKER_NOT_FOUND));

        // 2. 관련 데이터 조회
        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new CustomException(ErrorCode.MACHINE_NOT_FOUND));
        Lot lot = lotRepository.findById(dto.getLotId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOT_NOT_FOUND));
        ProcessStep processStep = processStepRepository.findById(dto.getProcessStepId())
                .orElseThrow(() -> new RuntimeException("공정 없음"));

        // 3. 로그 저장
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

        // 4. 재고 자동 업데이트 (양품이 있을 경우만)
        if (dto.getGoodQty() > 0) {
            inventoryService.addStockFromProduction(lot, dto.getGoodQty());
        }
    }
}