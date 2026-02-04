package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.DefectType;
import com.final_project.battery.dto.response.DashboardDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MachineRepository machineRepository;
    private final SensorLogRepository sensorLogRepository;
    private final ProductionLogRepository productionLogRepository;
    private final DefectLogRepository defectLogRepository;
    private final WorkerRepository workerRepository;
    private final WorkOrderRepository workOrderRepository;

    @Transactional(readOnly = true)
    public DashboardDto getDashboardData() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 1. [상단 요약] 오늘 생산 실적 집계
        List<ProductionLog> pLogs = productionLogRepository.findByStartedAtBetween(startOfDay, endOfDay);

        int totalActual = pLogs.stream().mapToInt(log -> log.getGoodQty() != null ? log.getGoodQty() : 0).sum();
        int totalDefect = pLogs.stream().mapToInt(log -> log.getBadQty() != null ? log.getBadQty() : 0).sum();
        int totalProduction = totalActual + totalDefect;

        // 계획 수량 (진행 중인 작업지시들의 목표 합계)
        int totalPlanned = workOrderRepository.findAll().stream()
                .filter(wo -> "IN_PROGRESS".equals(wo.getStatus().name()))
                .mapToInt(WorkOrder::getPlannedQty)
                .sum();
        if (totalPlanned == 0) totalPlanned = 1000; // 0으로 나누기 방지용 기본값

        double achievementRate = (double) totalActual / totalPlanned * 100;
        double actualRate = totalProduction == 0 ? 0 : (double) totalActual / totalProduction * 100;
        double defectRate = totalProduction == 0 ? 0 : (double) totalDefect / totalProduction * 100;

        DashboardDto.SummaryDto summary = DashboardDto.SummaryDto.builder()
                .achievementRate(round(achievementRate))
                .totalActual(totalActual)
                .actualRate(round(actualRate))
                .totalDefect(totalDefect)
                .defectRate(round(defectRate))
                .build();

        // 2. [차트] 시간별 생산 현황 (09:00 ~ 18:00)
        Map<Integer, List<ProductionLog>> groupedByHour = pLogs.stream()
                .filter(log -> log.getStartedAt() != null)
                .collect(Collectors.groupingBy(log -> log.getStartedAt().getHour()));

        List<DashboardDto.ProductionTrendDto> trends = new ArrayList<>();
        Random random = new Random();

        for (int i = 9; i <= 18; i++) {
            int hourActual = 0;
            int hourDefect = 0;

            if (groupedByHour.containsKey(i)) {
                List<ProductionLog> hourLogs = groupedByHour.get(i);
                hourActual = hourLogs.stream().mapToInt(l -> l.getGoodQty() != null ? l.getGoodQty() : 0).sum();
                hourDefect = hourLogs.stream().mapToInt(l -> l.getBadQty() != null ? l.getBadQty() : 0).sum();
            } else if (i < LocalDateTime.now().getHour()) {
                // 과거 시간이지만 데이터가 없으면 그래프가 비어보이니 약간의 더미 데이터 추가 (선택사항)
                hourActual = random.nextInt(50) + 10;
                hourDefect = random.nextInt(5);
            }

            int hourTotal = hourActual + hourDefect;
            double hourDefectRate = hourTotal == 0 ? 0 : (double) hourDefect / hourTotal * 100;

            trends.add(DashboardDto.ProductionTrendDto.builder()
                    .time(String.format("%02d:00", i))
                    .actual(hourActual)
                    .defect(hourDefect)
                    .defectRate(round(hourDefectRate))
                    .build());
        }

        // 3. [파이차트] 불량 유형 분석
        List<DefectLog> dLogs = defectLogRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        Map<DefectType, Long> defectCounts = dLogs.stream()
                .collect(Collectors.groupingBy(DefectLog::getDefectType, Collectors.counting()));

        List<DashboardDto.DefectAnalysisDto> defectAnalysis = defectCounts.entrySet().stream()
                .map(entry -> DashboardDto.DefectAnalysisDto.builder()
                        .name(entry.getKey().name())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        if (defectAnalysis.isEmpty()) {
            defectAnalysis.add(DashboardDto.DefectAnalysisDto.builder().name("NONE").value(1L).build());
        }

        // 4. [설비 리스트] 가동 현황 (C# 센서 데이터 연동)
        List<Machine> machines = machineRepository.findAll();
        List<DashboardDto.MachineStatusDto> machineStatusList = new ArrayList<>();

        for (Machine m : machines) {
            SensorLog sensor = sensorLogRepository.findTopByMachineOrderByRecordedAtDesc(m).orElse(null);

            machineStatusList.add(DashboardDto.MachineStatusDto.builder()
                    .machineCode(m.getMachineCode())
                    .machineName(m.getMachineName())
                    .status(m.getStatus().name())
                    .temperature(sensor != null ? round(sensor.getTemperature()) : 0.0)
                    .humidity(sensor != null ? round(sensor.getHumidity()) : 0.0)
                    .voltage(sensor != null ? round(sensor.getVoltage()) : 0.0)
                    .build());
        }

        // 5. [작업자]
        long totalWorkers = workerRepository.count();
        long activeWorkers = 0;
        // Worker 엔티티에 isActive 필드가 Boolean 타입이므로 체크
        // (WorkerRepository에 countByIsActiveTrue 메서드가 없으면 전체 가져와서 필터링)
        activeWorkers = workerRepository.findAll().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()))
                .count();

        DashboardDto.WorkerInfoDto workerInfo = DashboardDto.WorkerInfoDto.builder()
                .total(totalWorkers)
                .working(activeWorkers)
                .standby(totalWorkers - activeWorkers)
                .build();

        // 6. [OEE] 공정 효율 (계산 + 더미)
        // 가동률(Availability) = 95% 고정 (데모용)
        double availability = 95.0;
        // 성능(Performance) = 계획 대비 실제 생산 비율
        double performance = totalPlanned > 0 ? (double) totalProduction / totalPlanned * 100 : 85.0;
        // OEE 계산
        double oee = (availability * performance * (actualRate == 0 ? 100 : actualRate)) / 10000;

        DashboardDto.ProcessEffDto processEff = DashboardDto.ProcessEffDto.builder()
                .availability(availability)
                .performance(round(performance))
                .defectRate(round(defectRate))
                .oee(round(oee))
                .materialUsage(round(45.0 + random.nextDouble() * 10)) // 자재 소모율은 45~55% 랜덤
                .build();

        return DashboardDto.builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .summary(summary)
                .productionTrend(trends)
                .defectAnalysis(defectAnalysis)
                .machineStatus(machineStatusList)
                .workerInfo(workerInfo)
                .processEff(processEff)
                .build();
    }

    // 소수점 첫째자리 반올림 유틸
    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}