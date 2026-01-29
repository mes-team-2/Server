package com.final_project.battery.controller;

import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workorder")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createWorkOrder(@RequestBody WorkOrderCreateDto dto) {
        Long workOrderId = workOrderService.createWorkOrder(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "작업지시가 성공적으로 등록되었습니다.");
        response.put("workOrderId", workOrderId);

        return ResponseEntity.ok(response);
    }
}