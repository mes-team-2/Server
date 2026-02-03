package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.InventoryStatus;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.MaterialLotStatus;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.response.MachineMaterialDto;
import com.final_project.battery.dto.response.MachineResponseDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineService {

    private final MachineRepository machineRepository;
    private final MaterialLotRepository materialLotRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LotRepository lotRepository;
    private final FgInventoryRepository fgInventoryRepository;

    // 설비 전체 목록 조회
    @Transactional(readOnly = true)
    public List<MachineResponseDto> getAllMachines() {
        return machineRepository.findAll().stream()
                .map(MachineResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MachineMaterialDto> getMountedMaterials(String machineCode) {
        // 1. 설비 조회
        Machine machine = machineRepository.findByMachineCode(machineCode)
                .orElseThrow(() -> new RuntimeException("설비 없음: " + machineCode));

        // 2. 장착된 자재 목록 조회 (List)
        List<MaterialLot> loadedLots = materialLotRepository.findByCurrentMachineAndStatus(machine, MaterialLotStatus.AVAILABLE);

        // 3. DTO 변환
        return loadedLots.stream()
                .map(lot -> MachineMaterialDto.builder()
                        .machineCode(machineCode)
                        .materialLotId(lot.getMaterialLotId())
                        .materialName(lot.getMaterial().getMaterialName())
                        .materialCode(lot.getMaterial().getMaterialCode())
                        .remainQty(lot.getRemainQty().doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void completeWorkOrder(String machineCode, String workOrderNo, int actualQty) { // int actualQty 추가
        WorkOrder wo = workOrderRepository.findByWorkOrderNo(workOrderNo)
                .orElseThrow(() -> new RuntimeException("작업지시 없음"));

        Machine machine = machineRepository.findByMachineCode(machineCode)
                .orElseThrow(() -> new RuntimeException("설비 없음"));

        boolean isFinalProcess = "PROC-50".equals(machine.getProcessCode()) || "MAC-A-05".equals(machineCode);

        if (isFinalProcess) {
            log.info("🏭 최종 공정({}) 완료. 실적 기반 완제품 입고...", machineCode);

            Lot lot = lotRepository.findFirstByWorkOrder(wo)
                    .orElseThrow(() -> new RuntimeException("Lot 없음"));

            lot.setStatus(LotStatus.COMPLETED);
            lot.setLotQty(actualQty); // [수정] Lot의 최종 수량도 실적으로 업데이트

            wo.setStatus(WorkOrderStatus.DONE);
            wo.setEndedAt(LocalDateTime.now());

            // [핵심 수정] 계획 수량(100)이 아니라 실제 만든 수량(예: 112)으로 입고
            FgInventory fg = FgInventory.builder()
                    .product(wo.getProduct())
                    .lot(lot)
                    .stockQty(actualQty) // <--- 여기가 핵심입니다!
                    .locationCode("WH-FG-A01")
                    .status(InventoryStatus.AVAILABLE)
                    .build();

            fgInventoryRepository.save(fg);
            log.info("🎉 완제품 입고 완료! [제품:{}] [실적:{}]", wo.getProduct().getProductName(), actualQty);
        } else {
            log.info("🔧 중간 공정({}) 배치 완료.", machineCode);
        }
    }

    @Transactional
    public MachineMaterialDto replaceMaterial(String machineCode, Long materialId) {
        Machine machine = machineRepository.findByMachineCode(machineCode)
                .orElseThrow(() -> new RuntimeException("설비 없음"));

        // 1. 현재 장착된(다 쓴) 자재 찾기 -> 상태 변경 (EXHAUSTED) & 탈착
        List<MaterialLot> currentLots = materialLotRepository.findByCurrentMachineAndStatus(machine, MaterialLotStatus.AVAILABLE);

        for (MaterialLot lot : currentLots) {
            if (lot.getMaterial().getMaterialId().equals(materialId)) {
                // 잔량이 남았어도 교체 요청이 오면 교체 (혹은 0인지 체크)
                lot.setCurrentMachine(null); // 탈착
                lot.setStatus(MaterialLotStatus.EXHAUSTED); // 소진 처리
                materialLotRepository.save(lot);
                log.info("♻️ 자재 탈착: {} (설비: {})", lot.getMaterialLotNo(), machineCode);
            }
        }

        // 2. 창고에 있는 새 자재 찾기 (FIFO: 먼저 들어온 순서)
        // 같은 자재(MaterialId)이면서, 장착되지 않았고(currentMachine is null), 상태가 AVAILABLE인 것
        MaterialLot newLot = materialLotRepository.findFirstByMaterial_MaterialIdAndCurrentMachineIsNullAndStatusOrderByInputDateAsc(
                        materialId, MaterialLotStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("교체할 여분 자재가 없습니다! (자재ID: " + materialId + ")"));

        // 3. 새 자재 장착
        newLot.setCurrentMachine(machine);
        materialLotRepository.save(newLot);
        log.info("✅ 자재 장착: {} -> {}", newLot.getMaterialLotNo(), machineCode);

        // 4. 결과 반환
        return MachineMaterialDto.builder()
                .machineCode(machineCode)
                .materialLotId(newLot.getMaterialLotId())
                .materialName(newLot.getMaterial().getMaterialName())
                .materialCode(newLot.getMaterial().getMaterialCode())
                .remainQty(newLot.getRemainQty().doubleValue())
                .build();
    }
}