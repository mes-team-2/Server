package com.final_project.battery.controller;

import com.final_project.battery.dto.request.LotCreateDto;
import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    // 작업 지시 등록 API
    @PostMapping("/create")
    public ResponseEntity<String> createWorkOrder(@RequestBody WorkOrderCreateDto dto) {
        String woNo = workOrderService.createWorkOrder(dto);
        return ResponseEntity.ok("작업 지시 생성 완료 (작업번호: " + woNo + ")");
    }

//    // LOT 발행 API
//    @PostMapping("/lot/create")
//    public ResponseEntity<String> createLot(@RequestBody LotCreateDto dto) {
//        String lotNo = workOrderService.createLot(dto);
//        return ResponseEntity.ok("LOT 발행 완료 (LOT번호: " + lotNo + ")");
//    }
}
