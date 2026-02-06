package com.final_project.battery.controller;

import com.final_project.battery.dto.response.ProcessProductionStatsDto;
import com.final_project.battery.dto.response.ProductionReportResponseDto;
import com.final_project.battery.service.ProductionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ProductionReportController {

    private final ProductionReportService productionReportService;

    // 일자별/제품별 생산 리포트
    @GetMapping("/production")
    public List<ProductionReportResponseDto> getProductionReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return productionReportService.getDailyReport(startDate, endDate);
    }

    // 오늘 공정별 현황 (차트용)
    @GetMapping("/process-today")
    public List<ProcessProductionStatsDto> getTodayProcessStats() {
        return productionReportService.getTodayProcessStats();
    }
}