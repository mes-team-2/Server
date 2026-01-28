package com.final_project.battery.controller;

import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.response.MachineMaterialDto;
import com.final_project.battery.repository.WorkOrderRepository;
import com.final_project.battery.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {
    private final MachineService machineService;
    private final WorkOrderRepository workOrderRepository;

    @GetMapping("/{machineCode}/workorder")
    public ResponseEntity<Map<String, Object>> getCurrentWorkOrder(@PathVariable String machineCode) {
        return workOrderRepository.findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus.IN_PROGRESS)
                .map(wo -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("workOrderNo", wo.getWorkOrderNo());
                    response.put("plannedQty", wo.getPlannedQty());
                    // [핵심] 설비가 BOM을 판단할 수 있게 제품 코드를 전달함
                    response.put("productCode", wo.getProduct().getProductCode());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Collections.singletonMap("message", "진행 중인 작업지시 없음")));
    }

    @GetMapping("/{machineCode}/material-lots")
    public ResponseEntity<List<MachineMaterialDto>> getMountedMaterials(@PathVariable String machineCode) {
        return ResponseEntity.ok(machineService.getMountedMaterials(machineCode));
    }

    @PostMapping("/{machineCode}/workorder/complete")
    public ResponseEntity<?> completeWorkOrder(@PathVariable String machineCode, @RequestBody Map<String, Object> body) {
        String workOrderNo = (String) body.get("workOrderNo");
        // [수정] 실제 생산량(actualQty)을 받아서 서비스로 넘김 (없으면 0 처리)
        int actualQty = body.containsKey("actualQty") ? Integer.parseInt(body.get("actualQty").toString()) : 0;

        machineService.completeWorkOrder(machineCode, workOrderNo, actualQty);

        return ResponseEntity.ok("작업지시(" + workOrderNo + ") 완료 처리 (실적: " + actualQty + ")");
    }

    @PostMapping("/{machineCode}/materials/{materialId}/replace")
    public ResponseEntity<MachineMaterialDto> replaceMaterial(
            @PathVariable String machineCode,
            @PathVariable Long materialId) {

        MachineMaterialDto newLot = machineService.replaceMaterial(machineCode, materialId);
        return ResponseEntity.ok(newLot);
    }
}