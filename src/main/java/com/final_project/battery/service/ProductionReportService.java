package com.final_project.battery.service;

import com.final_project.battery.dto.response.ProcessProductionStatsDto;
import com.final_project.battery.dto.response.ProductionReportResponseDto;
import com.final_project.battery.repository.FgInventoryRepository;
import com.final_project.battery.repository.ProductionLogRepository;
import com.final_project.battery.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionReportService {

    private final WorkOrderRepository workOrderRepository;
    private final FgInventoryRepository fgInventoryRepository;
    private final ProductionLogRepository productionLogRepository;

    @Transactional(readOnly = true)
    public List<ProductionReportResponseDto> getDailyReport(String startDate, String endDate) {
        LocalDateTime start = (startDate != null)
                ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime end = (endDate != null)
                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        // 1. 계획 (WorkOrder)
        List<Object[]> plans = workOrderRepository.findDailyPlanStats(start, end);
        // 2. 양품 (FgInventory)
        List<Object[]> oks = fgInventoryRepository.findDailyProductionStats(start, end);
        // 3. 불량 (ProductionLog - 5공정)
        List<Object[]> ngs = productionLogRepository.findDailyDefectStats(start, end);

        // 데이터 병합을 위한 Map (Key: "Date|ProductName")
        Map<String, ProductionReportResponseDto.ProductionReportResponseDtoBuilder> map = new HashMap<>();

        // plans: [date, product, sumPlan]
        for (Object[] row : plans) {
            String key = row[0] + "|" + row[1];
            map.computeIfAbsent(key, k -> ProductionReportResponseDto.builder()
                            .date((String) row[0])
                            .productName((String) row[1])
                            .plan(0L).ok(0L).ng(0L).prod(0L))
                    .plan(row[2] != null ? ((Number) row[2]).longValue() : 0L);
        }

        // oks: [date, product, sumOk]
        for (Object[] row : oks) {
            String key = row[0] + "|" + row[1];
            map.computeIfAbsent(key, k -> ProductionReportResponseDto.builder()
                            .date((String) row[0])
                            .productName((String) row[1])
                            .plan(0L).ok(0L).ng(0L).prod(0L))
                    .ok(row[2] != null ? ((Number) row[2]).longValue() : 0L);
        }

        // ngs: [date, product, sumNg]
        for (Object[] row : ngs) {
            String key = row[0] + "|" + row[1];
            map.computeIfAbsent(key, k -> ProductionReportResponseDto.builder()
                            .date((String) row[0])
                            .productName((String) row[1])
                            .plan(0L).ok(0L).ng(0L).prod(0L))
                    .ng(row[2] != null ? ((Number) row[2]).longValue() : 0L);
        }

        // 최종 계산 (Prod = OK + NG, Yield, DefectRate)
        return map.values().stream().map(b -> {
                    ProductionReportResponseDto dto = b.build();
                    long prod = dto.getOk() + dto.getNg();
                    double yield = prod == 0 ? 0.0 : Math.round(((double) dto.getOk() / prod) * 1000) / 10.0;
                    double defectRate = prod == 0 ? 0.0 : Math.round(((double) dto.getNg() / prod) * 1000) / 10.0;

                    return ProductionReportResponseDto.builder()
                            .date(dto.getDate())
                            .productName(dto.getProductName())
                            .plan(dto.getPlan())
                            .prod(prod)
                            .ok(dto.getOk())
                            .ng(dto.getNg())
                            .yield(yield)
                            .defectRate(defectRate)
                            .build();
                }).sorted(Comparator.comparing(ProductionReportResponseDto::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcessProductionStatsDto> getTodayProcessStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> stats = productionLogRepository.findTodayProcessStats(start, end);

        return stats.stream().map(row -> ProcessProductionStatsDto.builder()
                        .processName((String) row[0])
                        .output(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                        .ng(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());
    }
}