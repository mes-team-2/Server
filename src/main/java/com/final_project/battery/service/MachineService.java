package com.final_project.battery.service;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.MaterialLot;
import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.MaterialLotStatus;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.response.MachineMaterialDto;
import com.final_project.battery.repository.MachineRepository;
import com.final_project.battery.repository.MaterialLotRepository;
import com.final_project.battery.repository.WorkOrderRepository;
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
    public void completeWorkOrder(String workOrderNo) {
        WorkOrder wo = workOrderRepository.findByWorkOrderNo(workOrderNo)
                .orElseThrow(() -> new RuntimeException("작업지시 없음"));

        // 상태 변경: IN_PROGRESS -> DONE
        wo.setStatus(WorkOrderStatus.DONE);
        wo.setDueAt(LocalDateTime.now()); // 종료 시간 기록

        workOrderRepository.save(wo);

        log.info("🎉 작업지시 [{}] 완료 처리됨 (Status: DONE)", workOrderNo);
    }
}