package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.*;
import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.dto.request.SensorLogRequestDto;
import com.final_project.battery.dto.response.*;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final MachineRepository machineRepository;
    private final SensorLogRepository sensorLogRepository;
    private final ProductionLogRepository productionLogRepository;
    private final DefectLogRepository defectLogRepository;
    private final MaterialLotRepository materialLotRepository;
    private final MaterialTxRepository materialTxRepository;
    private final BomRepository bomRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ProcessLogRepository processLogRepository;
    private final LotRepository lotRepository;
    private final WorkerRepository workerRepository;
    private final ProcessStepRepository processStepRepository;
    private final QualityTestRepository qualityTestRepository;
    private final MachineStatusLogRepository machineStatusLogRepository;

    // 공정 이력 검색 서비스
    @Transactional(readOnly = true)
    public List<ProcessLogResponseDto> searchProcessLogs(String startDate, String endDate, String keyword) {
        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate).atTime(23, 59, 59) : null;

        // 1. DB에서 조건에 맞는 모든 로그 조회 (중복 포함)
        List<ProcessLog> rawLogs = processLogRepository.search(start, end, keyword);

        // 2. [핵심] LOT ID + 공정 ID 기준으로 그룹핑
        Map<String, List<ProcessLog>> groupedLogs = rawLogs.stream()
                .collect(Collectors.groupingBy(log ->
                        log.getLot().getLotId() + "_" + log.getProcessStep().getProcessStepId()
                ));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 3. 그룹별 데이터 집계 및 DTO 변환
        return groupedLogs.values().stream().map(group -> {
                    // 대표 로그 (가장 최근 것 사용)
                    ProcessLog representative = group.get(0);

                    // 시간 범위 계산 (최초 시작 ~ 최종 종료)
                    LocalDateTime startTime = group.stream()
                            .map(ProcessLog::getStartTime).filter(Objects::nonNull)
                            .min(LocalDateTime::compareTo).orElse(null);
                    LocalDateTime endTime = group.stream()
                            .map(ProcessLog::getEndTime).filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo).orElse(null);

                    // 해당 LOT + 공정의 모든 생산 실적 조회 (58개 등 N개 레코드)
                    List<ProductionLog> prodList = productionLogRepository.findByLotAndProcessStep(
                            representative.getLot(), representative.getProcessStep());

                    // 수량 및 센서값 집계
                    int totalGood = 0;
                    int totalBad = 0;
                    double avgTemp = 0.0;
                    double avgHumid = 0.0;
                    double avgVolt = 0.0;

                    if (!prodList.isEmpty()) {
                        totalGood = prodList.stream().mapToInt(p -> p.getGoodQty() != null ? p.getGoodQty() : 0).sum();
                        totalBad = prodList.stream().mapToInt(p -> p.getBadQty() != null ? p.getBadQty() : 0).sum();

                        avgTemp = prodList.stream().mapToDouble(p -> p.getTemperature() != null ? p.getTemperature() : 0).average().orElse(0.0);
                        avgHumid = prodList.stream().mapToDouble(p -> p.getHumidity() != null ? p.getHumidity() : 0).average().orElse(0.0);
                        avgVolt = prodList.stream().mapToDouble(p -> p.getVoltage() != null ? p.getVoltage() : 0).average().orElse(0.0);
                    }

                    return ProcessLogResponseDto.builder()
                            .id(representative.getProcessLogId())
                            .lotNo(representative.getLot().getLotNo())
                            .processStep(representative.getProcessStep().getStepName())
                            .machineName(representative.getMachine().getMachineName())
                            .workerName(representative.getWorker() != null ? representative.getWorker().getWorkerName() : "-")
                            .status(representative.getStatus().name()) // 최신 상태 기준
                            .startTime(startTime != null ? startTime.format(fmt) : "-")
                            .endTime(endTime != null ? endTime.format(fmt) : "-")
                            // [집계된 데이터]
                            .goodQty(totalGood)
                            .badQty(totalBad)
                            .temperature(Math.round(avgTemp * 10) / 10.0)
                            .humidity(Math.round(avgHumid * 10) / 10.0)
                            .voltage(Math.round(avgVolt * 10) / 10.0)
                            .build();

                })
                // 시작 시간 내림차순 정렬 (최신순)
                .sorted(Comparator.comparing(ProcessLogResponseDto::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    // 1. 센서 로그 저장
    @Transactional
    public void saveSensorLog(SensorLogRequestDto dto) {
        Machine machine = machineRepository.findByMachineCode(dto.getMachineCode())
                .orElseThrow(() -> new RuntimeException("미등록 설비: " + dto.getMachineCode()));

        sensorLogRepository.save(SensorLog.builder()
                .machine(machine)
                .temperature(dto.getData().getTemperature())
                .humidity(dto.getData().getHumidity())
                .voltage(dto.getData().getVoltage())
                .recordedAt(LocalDateTime.parse(dto.getTimestamp()))
                .build());
    }

    // 2. 생산 실적 처리
    @Transactional
    public void saveProductionLog(ProductionLogRequestDto dto) {
        Machine machine = machineRepository.findByMachineCode(dto.getMachineCode())
                .orElseThrow(() -> new RuntimeException("미등록 설비"));

        Worker worker = workerRepository.findByWorkerCode(dto.getWorkerCode()).orElse(null);

        // (1) 현재 작업지시 찾기
        WorkOrder workOrder = workOrderRepository.findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus.IN_PROGRESS)
                .orElseThrow(() -> new RuntimeException("진행 중인 작업지시 없음"));

        // (2) Lot 찾기 (새로 만들지 않고 기존 Lot 사용)
        Lot productLot = lotRepository.findFirstByWorkOrder(workOrder)
                .orElseThrow(() -> new RuntimeException("작업지시에 해당하는 Lot가 없습니다."));

        // (3) 자재 차감
        StringBuilder materialSnapshot = new StringBuilder();
        if (dto.getMaterialLotIds() != null) {
            for (Long matLotId : dto.getMaterialLotIds()) {
                processMaterialConsumption(matLotId, dto.getQty(), workOrder, productLot, materialSnapshot);
            }
        }

        // (4) 설비 코드로 공정 단계(ProcessStep) 찾기 [NULL 해결 핵심]
        ProcessStep step = processStepRepository.findByStepCode(machine.getProcessCode())
                .orElseThrow(() -> new RuntimeException("공정 정보 없음: " + machine.getProcessCode()));

        // (5) ProductionLog 저장
        ProductionLog prodLog = new ProductionLog();
        prodLog.setMachine(machine);
        prodLog.setWorkOrder(workOrder);
        prodLog.setLot(productLot);
        prodLog.setProcessStep(step);
        prodLog.setWorker(worker);
        prodLog.setGoodQty(dto.getIsBad() ? 0 : dto.getQty());
        prodLog.setBadQty(dto.getIsBad() ? dto.getQty() : 0);
        prodLog.setTemperature(dto.getTemperature());
        prodLog.setHumidity(dto.getHumidity());
        prodLog.setVoltage(dto.getVoltage());
        prodLog.setUsedMaterialInfo(materialSnapshot.toString());
        prodLog.setStartedAt(LocalDateTime.parse(dto.getTimestamp()).minusSeconds(2));
        prodLog.setEndedAt(LocalDateTime.parse(dto.getTimestamp()));
        productionLogRepository.save(prodLog);

        ProcessLog processLog = new ProcessLog();
        processLog.setLot(productLot);
        processLog.setProcessStep(step);
        processLog.setMachine(machine);
        processLog.setWorker(worker);
        processLog.setStartTime(prodLog.getStartedAt());
        processLog.setEndTime(prodLog.getEndedAt());
        processLog.setStatus(dto.getIsBad() ? ProcessLogStatus.FAIL : ProcessLogStatus.PASS);
        processLogRepository.save(processLog);

        if (dto.getIsBad()) {
            saveDefectLog(dto, prodLog, machine, worker, productLot);
        }

        log.info("✅ 생산 기록: [Lot:{}] [설비:{}] [공정:{}] [Temp:{}C]",
                productLot.getLotNo(), machine.getMachineCode(), step.getStepName(), dto.getTemperature());
    }

    @Transactional
    public void saveMachineStatusLog(Map<String, String> body) {
        String machineCode = body.get("machineCode");
        String newStatus = body.get("status"); // RUN, STOP, WAIT, DOWN
        String reason = body.getOrDefault("reason", "");
        String workerCode = body.get("workerCode");

        Machine machine = machineRepository.findByMachineCode(machineCode)
                .orElseThrow(() -> new RuntimeException("설비 없음"));

        // 상태가 이전과 동일하면 로그를 쌓지 않음 (선택 사항, 여기선 무조건 남기거나 비즈니스 로직에 따름)
        // 하지만 시뮬레이터가 '변경될 때만' 보내기로 약속하면 그냥 저장하면 됩니다.

        // 1. Machine 마스터 정보 업데이트 (현재 상태 갱신)
        try {
            machine.setStatus(MachineStatus.valueOf(newStatus));
        } catch (Exception e) {
            log.warn("알 수 없는 상태 코드: {}", newStatus);
        }

        // 2. 로그 저장
        MachineStatusLog logEntity = new MachineStatusLog();
        logEntity.setMachine(machine);

        // 작업자 정보가 있으면 넣고, 없으면 SYSTEM 처리
        if (workerCode != null && !workerCode.equals("UNKNOWN")) {
            workerRepository.findByWorkerCode(workerCode).ifPresent(logEntity::setWorker);
        }

        logEntity.setStatus(newStatus);
        logEntity.setReasonCode(reason);
        logEntity.setStartTime(LocalDateTime.now());
        // endTime은 다음 상태가 들어올 때 업데이트하는 방식이 정석이나,
        // 단순 로그형(이벤트형) 기록이라면 startTime만 있어도 분석 가능합니다.

        machineStatusLogRepository.save(logEntity);

        log.info("🚦 설비 상태 변경: [{}] {} -> {} ({})",
                machineCode, machine.getStatus(), newStatus, reason);
    }

    private void processMaterialConsumption(Long matLotId, Integer prodQty, WorkOrder wo, Lot productLot, StringBuilder snapshot) {
        MaterialLot matLot = materialLotRepository.findById(matLotId)
                .orElseThrow(() -> new RuntimeException("자재 Lot 없음"));

        // 1. 해당 제품(Product)과 자재(Material)에 맞는 BOM 정보 조회
        BOM bom = bomRepository.findByProductAndMaterial(wo.getProduct(), matLot.getMaterial())
                .orElse(null);

        // 2. BOM이 있으면 그 수량만큼, 없으면 0 (혹은 1) 처리
        BigDecimal requiredPerUnit = (bom != null) ? bom.getRequiredQty() : BigDecimal.ZERO;

        // 3. 생산수량 * 단위소모량 = 총 소모량
        BigDecimal consumeQty = requiredPerUnit.multiply(BigDecimal.valueOf(prodQty));

        // 4. 재고 차감 (0보다 클 때만)
        if (consumeQty.compareTo(BigDecimal.ZERO) > 0) {
            matLot.setRemainQty(matLot.getRemainQty().subtract(consumeQty));

            MaterialTx tx = MaterialTx.builder()
                    .material(matLot.getMaterial())
                    .materialLot(matLot)
                    .workOrder(wo)
                    .lot(productLot)
                    .qty(consumeQty)
                    .txType(TxType.CONSUME)
                    .txTime(LocalDateTime.now())
                    .build();
            materialTxRepository.save(tx);

            if (snapshot.length() > 0) snapshot.append(", ");
            snapshot.append(matLot.getMaterial().getMaterialName())
                    .append("(").append(consumeQty).append(matLot.getMaterial().getUnit()).append(")");
        }
    }

    private void saveDefectLog(ProductionLogRequestDto dto, ProductionLog prodLog, Machine machine, Worker worker, Lot lot) {
        DefectLog defect = new DefectLog();
        defect.setProductionLog(prodLog);
        defect.setMachine(machine);
        defect.setWorker(worker);
        defect.setLot(lot);
        defect.setDefectQty(dto.getQty());
        try { defect.setDefectType(DefectType.valueOf(dto.getDefectType())); } catch (Exception e) { defect.setDefectType(DefectType.ETC); }
        defect.setTemperature(dto.getTemperature());
        defect.setHumidity(dto.getHumidity());
        defect.setVoltage(dto.getVoltage());
        defect.setCreatedAt(LocalDateTime.now());
        defectLogRepository.save(defect);
    }

    // 검사 이력 조회
    public Page<TestLogResponseDto> searchTestLogs(
            Boolean isOk,
            String keyword,
            DefectType defectType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return productionLogRepository.searchTestLogs(
                isOk,
                keyword,
                defectType,
                startDate,
                endDate,
                pageable
        );
    }

    // 검사이력 집계 조합
    public TestLogDashboardResponseDto getDashboard(
            Boolean isOk,
            String keyword,
            DefectType defectType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        /* =========================
           1. 카드 집계
        ========================= */
        List<Object[]> cardList = productionLogRepository.getCardSummary(
                isOk, keyword, defectType, startDate, endDate
        );

        Object[] card = cardList.isEmpty() ? null : cardList.get(0);

        Long totalCount = card != null && card[0] != null
                ? ((Number) card[0]).longValue()
                : 0L;

        Long okCount = card != null && card[1] != null
                ? ((Number) card[1]).longValue()
                : 0L;

        Long ngCount = card != null && card[2] != null
                ? ((Number) card[2]).longValue()
                : 0L;

        int okRate = totalCount == 0
                ? 0
                : (int) ((okCount * 100) / totalCount);

        String topDefectType = "-";


        /* =========================
           2. 일자별 OK / NG
        ========================= */
        List<Object[]> dailyRaw = productionLogRepository.getDailySummary(
                isOk, keyword, defectType, startDate, endDate
        );

        List<TestLogDashboardResponseDto.Daily> daily =
                dailyRaw.stream()
                        .map(r -> new TestLogDashboardResponseDto.Daily(
                                (String) r[0],                     // day
                                r[1] != null ? ((Number) r[1]).longValue() : 0L,
                                r[2] != null ? ((Number) r[2]).longValue() : 0L // ng
                        ))
                        .collect(Collectors.toList());


        /* =========================
           3. 불량 유형 집계
        ========================= */
        List<Object[]> defectRaw = productionLogRepository.getDefectSummary(
                isOk, keyword, defectType, startDate, endDate
        );

        List<TestLogDashboardResponseDto.Defect> defects =
                defectRaw.stream()
                        .map(r -> new TestLogDashboardResponseDto.Defect(
                                r[0] != null ? r[0].toString() : "UNKNOWN",
                                r[1] != null ? ((Number) r[1]).longValue() : 0L
                        ))
                        .collect(Collectors.toList());

        // 최다 불량 추출
        if (!defects.isEmpty()) {
            topDefectType = defects.get(0).getDefectType();
        }


        /* =========================
           4. DTO 조립
        ========================= */
        return new TestLogDashboardResponseDto(
                totalCount,
                okCount,
                ngCount,
                okRate,
                topDefectType,
                daily,
                defects
        );
    }

    // 추적로그 조회
    public Page<TraceabilityResponseDto> searchTrace(
            String keyword,
            String machine,
            String process,
            String material,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {

        Sort newSort = Sort.unsorted();

        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = new ArrayList<>();

            for (Sort.Order order : pageable.getSort()) {
                String prop = order.getProperty();

                switch (prop) {
                    case "lot":
                        orders.add(new Sort.Order(order.getDirection(), "lotNo"));
                        break;

                    // testedAt 제거
                    // yieldRate 제거
                }
            }

            if (!orders.isEmpty()) {
                newSort = Sort.by(orders);
            }
        }

        Pageable newPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                newSort
        );

        return qualityTestRepository.searchTrace(
                keyword,
                machine,
                process,
                material,
                start,
                end,
                newPageable
        );
    }


    // 추적성 집계
    public TraceSummaryResponseDto getTraceSummary(
            String keyword,
            String machine,
            String process,
            String material,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return qualityTestRepository.searchTraceSummary(
                keyword,
                machine,
                process,
                material,
                start,
                end
        );
    }
}