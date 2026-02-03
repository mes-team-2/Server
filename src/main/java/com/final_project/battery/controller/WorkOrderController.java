package com.final_project.battery.controller;

import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.dto.response.WorkOrderDetailDto;
import com.final_project.battery.dto.response.WorkOrderResponseDto;
import com.final_project.battery.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workorder")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    // 작업지시 목록 조회
    @GetMapping
    public ResponseEntity<List<WorkOrderResponseDto>> getWorkOrderList() {
        return ResponseEntity.ok(workOrderService.getWorkOrderList());
    }

    // 작업 시작
    @PostMapping
    public ResponseEntity<Map<String, Object>> createWorkOrder(@RequestBody WorkOrderCreateDto dto) {
        Long workOrderId = workOrderService.createWorkOrder(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "작업지시가 성공적으로 등록되었습니다.");
        response.put("workOrderId", workOrderId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workOrderNo}/start")
    public ResponseEntity<String> startWorkOrder(@PathVariable String workOrderNo) {
        workOrderService.startWorkOrder(workOrderNo);
        return ResponseEntity.ok("작업이 시작되었습니다.");
    }

//    작업지시 상세 조회
    @GetMapping("/{workOrderNo}")
    public ResponseEntity<WorkOrderDetailDto> getWorkOrderDetail(@PathVariable String workOrderNo) {
        return ResponseEntity.ok(workOrderService.getWorkOrderDetail(workOrderNo));
    }
}