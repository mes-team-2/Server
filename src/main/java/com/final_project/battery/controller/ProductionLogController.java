package com.final_project.battery.controller;

import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionLogController {
    private final ProductionService productionService;

    // C#
//    @PostMapping("/log")
//    public ResponseEntity<String> saveLog(@RequestBody ProductionLogRequestDto requestDto) {
//        productionService.saveProductionLog(requestDto);
//        return ResponseEntity.ok("생산 실적 수집 완료");
//    }
}
