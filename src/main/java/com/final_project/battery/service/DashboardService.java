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

    private static final String FINAL_PROCESS_CODE = "PROC-050";

    @Transactional(readOnly = true)
    public DashboardDto getDashboardData() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 1. [상단 요약] 오늘 생산 실적 집계
        List<ProductionLog> pLogs = productionLogRepository.findByStartedAtBetween(startOfDay, endOfDay);

        // [수정] 양품 수량(Total Actual)은 '최종 공정(PROC-50)'의 실적만 합산
        int totalActual = pLogs.stream()
                .filter(log -> log.getProcessStep() != null && FINAL_PROCESS_CODE.equals(log.getProcessStep().getStepCode()))
                .mapToInt(log -> log.getGoodQty() != null ? log.getGoodQty() : 0)
                .sum();

        // 불량 수량(Total Defect)은 '모든 공정'의 불량을 합산 (전체 손실 파악용)
        int totalDefect = pLogs.stream()
                .filter(log -> log.getProcessStep() != null && FINAL_PROCESS_CODE.equals(log.getProcessStep().getStepCode()))
                .mapToInt(log -> log.getBadQty() != null ? log.getBadQty() : 0)
                .sum();

        int totalProduction = totalActual + totalDefect;

        // 계획 수량 (진행 중인 작업지시)
//        int totalPlanned = workOrderRepository.findAll().stream()
//                .filter(wo -> "IN_PROGRESS".equals(wo.getStatus().name()))
//                .mapToInt(WorkOrder::getPlannedQty)
//                .sum();
//        if (totalPlanned == 0) totalPlanned = 1000;

        int totalPlanned = 600;

        double achievementRate = (double) totalActual / totalPlanned * 100;
        // 양품률 = (최종양품 / (최종양품 + 전체불량)) - 엄격하게 계산
        double actualRate = totalProduction == 0 ? 0 : (double) totalActual / (totalActual + totalDefect) * 100;
        double defectRate = totalProduction == 0 ? 0 : (double) totalDefect / (totalActual + totalDefect) * 100;

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

        for (int i = 9; i <= 18; i++) {
            int hourActual = 0;
            int hourDefect = 0;

            if (groupedByHour.containsKey(i)) {
                List<ProductionLog> hourLogs = groupedByHour.get(i);

                // [수정] 차트에서도 양품은 최종 공정 것만 표시
                hourActual = hourLogs.stream()
                        .filter(l -> l.getProcessStep() != null && FINAL_PROCESS_CODE.equals(l.getProcessStep().getStepCode()))
                        .mapToInt(l -> l.getGoodQty() != null ? l.getGoodQty() : 0)
                        .sum();

                // 불량은 해당 시간대 전체 공정 불량 표시
                hourDefect = hourLogs.stream()
                        .filter(l -> l.getProcessStep() != null && FINAL_PROCESS_CODE.equals(l.getProcessStep().getStepCode()))
                        .mapToInt(l -> l.getBadQty() != null ? l.getBadQty() : 0)
                        .sum();
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

        // 3. [파이차트] 불량 유형 분석 (DB 기반)
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

        // 4. [설비 리스트] 가동 현황 (최신 센서 데이터)
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
        long activeWorkers = workerRepository.findAll().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()))
                .count();

        DashboardDto.WorkerInfoDto workerInfo = DashboardDto.WorkerInfoDto.builder()
                .total(totalWorkers)
                .working(activeWorkers)
                .standby(totalWorkers - activeWorkers)
                .build();

        // 6. [OEE] 공정 효율
        long runningMachines = machines.stream().filter(m -> "RUN".equals(m.getStatus().name())).count();
        double availability = machines.isEmpty() ? 0 : (double) runningMachines / machines.size() * 100;
        double performance = totalPlanned > 0 ? (double) totalActual / totalPlanned * 100 : 0.0; // 성능도 최종 양품 기준
        double oee = (availability * performance * (actualRate == 0 ? 100 : actualRate)) / 10000;

        DashboardDto.ProcessEffDto processEff = DashboardDto.ProcessEffDto.builder()
                .availability(round(availability))
                .performance(round(performance))
                .defectRate(round(defectRate))
                .oee(round(oee))
                .materialUsage(round(45.0 + new Random().nextInt(5)))
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

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}