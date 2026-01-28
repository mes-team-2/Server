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
                    response.put("productName", wo.getProduct().getProductName());
                    response.put("plannedQty", wo.getPlannedQty());
                    response.put("producedQty", 0);
                    return ResponseEntity.ok(response);
                })
                // [수정] 에러 시에도 Map을 반환하여 타입 불일치 해결
                .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Collections.singletonMap("message", "진행 중인 작업지시 없음")));
    }

    @GetMapping("/{machineCode}/material-lots")
    public ResponseEntity<List<MachineMaterialDto>> getMountedMaterials(@PathVariable String machineCode) {
        return ResponseEntity.ok(machineService.getMountedMaterials(machineCode));
    }

    @PostMapping("/{machineCode}/workorder/complete")
    public ResponseEntity<?> completeWorkOrder(@PathVariable String machineCode, @RequestBody Map<String, String> body) {
        String workOrderNo = body.get("workOrderNo");
        machineService.completeWorkOrder(workOrderNo);
        return ResponseEntity.ok("작업지시(" + workOrderNo + ") 상태가 DONE으로 변경되었습니다.");
    }
}