package com.final_project.battery.controller;

import com.final_project.battery.domain.common.DefectType;
import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.dto.request.SensorLogRequestDto;
import com.final_project.battery.dto.response.ProcessLogResponseDto;
import com.final_project.battery.dto.response.ProductReportResponse;
import com.final_project.battery.dto.response.TestLogDashboardResponseDto;
import com.final_project.battery.dto.response.TestLogResponseDto;
import com.final_project.battery.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    // 공정 이력 조회 API
    @GetMapping("/process")
    public ResponseEntity<List<ProcessLogResponseDto>> getProcessLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(logService.searchProcessLogs(startDate, endDate, keyword));
    }

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


    // 검사 이력 조회
    @GetMapping("/testlog")
    public ResponseEntity<?> searchTestLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DefectType defectType,
            @RequestParam(required = false) Boolean isOk,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("endedAt").descending());

        Page<TestLogResponseDto> result =
                logService.searchTestLogs(
                        isOk,
                        keyword,
                        defectType,
                        startDate,
                        endDate,
                        pageable
                );
        log.info("defectType : {}]", defectType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/testlog/dashboard")
    public ResponseEntity<TestLogDashboardResponseDto> getDashboard(
            @RequestParam(required = false) Boolean isOk,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DefectType defectType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {

        TestLogDashboardResponseDto result =
                logService.getDashboard(isOk, keyword, defectType, startDate, endDate);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/productionReport")
    public Page<ProductReportResponse> getProductReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,

            @RequestParam(required = false)
            String productName,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("요청오냐 : {}", pageable);
        return logService.getProductReport(
                start,
                end,
                productName,
                pageable
        );
    }
}
