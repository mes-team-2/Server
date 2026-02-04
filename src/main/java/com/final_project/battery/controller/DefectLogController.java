package com.final_project.battery.controller;

import com.final_project.battery.dto.response.DefectLogTableDto;
import com.final_project.battery.service.DefectLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/defect-logs")

public class DefectLogController {

    private final DefectLogService defectLogService;

    /**
     * 불량 로그 테이블 조회
     * 예) /api/defect-logs?date=2026-02-01
     */
    @GetMapping
    public List<DefectLogTableDto> getDefectLogs(
            @RequestParam LocalDate date
    ) {
        // KST 기준 하루
        ZonedDateTime start = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime end = date.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Seoul"));

        return defectLogService.getDefectLogsForTable(
                start.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                end.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
        );
    }
}