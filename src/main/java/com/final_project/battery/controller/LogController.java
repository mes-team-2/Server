package com.final_project.battery.controller;

import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.dto.request.SensorLogRequestDto;
import com.final_project.battery.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    // 1. 센서 데이터 수신 (5초 주기)
    @PostMapping("/sensor")
    public ResponseEntity<String> receiveSensorLog(@RequestBody SensorLogRequestDto dto) {
        logService.saveSensorLog(dto);
        return ResponseEntity.ok("OK");
    }

    // 2. 생산/불량 실적 수신 (생산 시점마다)
    @PostMapping("/production")
    public ResponseEntity<String> receiveProductionLog(@RequestBody ProductionLogRequestDto dto) {
        try {
            logService.saveProductionLog(dto);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("생산 로그 저장 실패: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/status")
    public ResponseEntity<?> saveStatusLog(@RequestBody Map<String, String> body) {
        logService.saveMachineStatusLog(body);
        return ResponseEntity.ok("상태 변경 저장 완료");
    }

}
