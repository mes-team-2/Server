package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.response.ProductLotDetailDto;
import com.final_project.battery.dto.response.ProductLotResponseDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLotService {

    private final LotRepository lotRepository;
    private final ProcessLogRepository processLogRepository;
    private final MaterialTxRepository materialTxRepository;
    private final ProductionLogRepository productionLogRepository;
    private final ProcessStepRepository processStepRepository;

    // 1. LOT 목록 조회
    @Transactional(readOnly = true)
    public List<ProductLotResponseDto> searchLots(String keyword, String startDate, String endDate, String statusFilter) {
        List<Lot> allLots = lotRepository.findAll();

        // [핵심] 5공정(검사) 단계 식별
        List<ProcessStep> inspectionSteps = processStepRepository.findAll().stream()
                .filter(step -> step.getStepName().contains("검사") || step.getStepName().contains("Inspection"))
                .collect(Collectors.toList());

        return allLots.stream()
                .filter(lot -> filterByKeyword(lot, keyword))
                .filter(lot -> filterByDate(lot, startDate, endDate))
                .filter(lot -> filterByStatus(lot, statusFilter))
                .map(lot -> convertToDto(lot, inspectionSteps)) // DTO 변환 시 5공정 실적 집계
                .sorted(Comparator.comparing(ProductLotResponseDto::getId).reversed())
                .collect(Collectors.toList());
    }

    // 2. LOT 상세 조회 (기존 유지)
    @Transactional(readOnly = true)
    public ProductLotDetailDto getLotDetail(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        WorkOrder wo = lot.getWorkOrder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // [공정 이력]
        List<ProcessLog> pLogs = processLogRepository.findByLotOrderByStartTimeAsc(lot);
        Map<String, List<ProcessLog>> groupedProcess = pLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getProcessStep().getStepName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ProductLotDetailDto.ProcessProgressDto> pList = new ArrayList<>();
        long pIdCounter = 1;
        for (Map.Entry<String, List<ProcessLog>> entry : groupedProcess.entrySet()) {
            List<ProcessLog> logs = entry.getValue();
            ProcessLog first = logs.get(0);
            ProcessLog last = logs.get(logs.size() - 1);

            pList.add(ProductLotDetailDto.ProcessProgressDto.builder()
                    .id(pIdCounter++)
                    .stepName(entry.getKey())
                    .machineName(last.getMachine() != null ? last.getMachine().getMachineName() : "-")
                    .status(last.getStatus().name())
                    .startedAt(first.getStartTime() != null ? first.getStartTime().format(fmt) : "-")
                    .endedAt(last.getEndTime() != null ? last.getEndTime().format(fmt) : "-")
                    .build());
        }

        // [자재 이력]
        List<MaterialTx> mLogs = materialTxRepository.findByLotAndTxType(lot, TxType.CONSUME);
        Map<String, List<MaterialTx>> groupedMaterial = mLogs.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getMaterial().getMaterialName() + "|" +
                                (tx.getMaterialLot() != null ? tx.getMaterialLot().getMaterialLotNo() : "-") + "|" +
                                tx.getMaterial().getUnit(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ProductLotDetailDto.MaterialInputDto> mList = new ArrayList<>();
        long mIdCounter = 1;
        for (Map.Entry<String, List<MaterialTx>> entry : groupedMaterial.entrySet()) {
            List<MaterialTx> txs = entry.getValue();
            String[] keys = entry.getKey().split("\\|");

            BigDecimal totalQty = txs.stream()
                    .map(MaterialTx::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            mList.add(ProductLotDetailDto.MaterialInputDto.builder()
                    .id(mIdCounter++)
                    .materialName(keys[0])
                    .lotNo(keys[1])
                    .unit(keys[2])
                    .qty(totalQty.stripTrailingZeros().toPlainString())
                    .time(txs.get(0).getTxTime().format(fmt))
                    .build());
        }

        return ProductLotDetailDto.builder()
                .lotId(lot.getLotId())
                .lotNo(lot.getLotNo())
                .status(lot.getStatus().name())
                .currentQty(lot.getLotQty())
                .createdAt(lot.getCreatedAt().format(fmt))
                .workOrderNo(wo.getWorkOrderNo())
                .manager(wo.getManager() != null ? wo.getManager().getWorkerName() : "-")
                .workOrderCreatedAt(wo.getCreatedAt().format(fmt))
                .productName(wo.getProduct().getProductName())
                .workOrderStatus(wo.getStatus().name())
                .plannedQty(wo.getPlannedQty())
                .workOrderStartDate(wo.getStartedAt() != null ? wo.getStartedAt().format(fmt) : "-")
                .workOrderDueDate(wo.getDueDate() != null ? wo.getDueDate().format(fmt) : "-")
                .processList(pList)
                .materialList(mList)
                .build();
    }

    // --- Helper Methods ---

    // [수정] 5공정(검사) 실적 집계 로직 개선
    private ProductLotResponseDto convertToDto(Lot lot, List<ProcessStep> inspectionSteps) {
        List<ProductionLog> prodLogs = productionLogRepository.findByLot(lot);

        int currentQty = 0;
        int defectQty = 0;

        // "검사" 공정 로그만 필터링
        List<ProductionLog> inspectionLogs = prodLogs.stream()
                .filter(log -> inspectionSteps.contains(log.getProcessStep()))
                .collect(Collectors.toList());

        // 5공정 기록이 있을 때만 집계
        if (!inspectionLogs.isEmpty()) {
            // [수정] 1. 불량 수량: 합계(sum)
            defectQty = inspectionLogs.stream()
                    .mapToInt(log -> log.getBadQty() != null ? log.getBadQty() : 0)
                    .sum();

            // [수정] 2. 현재(양품) 수량: 합계(sum)로 변경
            // (로그가 개별 생산 건으로 쌓이는 경우 max는 1이 되므로 sum으로 변경)
            currentQty = inspectionLogs.stream()
                    .mapToInt(log -> log.getGoodQty() != null ? log.getGoodQty() : 0)
                    .sum();
        }

        return ProductLotResponseDto.of(lot, currentQty, defectQty);
    }

    private boolean filterByKeyword(Lot lot, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String k = keyword.toLowerCase();
        return lot.getLotNo().toLowerCase().contains(k) ||
                lot.getProduct().getProductName().toLowerCase().contains(k) ||
                lot.getWorkOrder().getWorkOrderNo().toLowerCase().contains(k);
    }

    private boolean filterByDate(Lot lot, String start, String end) {
        if (start == null || end == null) return true;
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        LocalDate lotDate = lot.getCreatedAt().toLocalDate();
        return !lotDate.isBefore(s) && !lotDate.isAfter(e);
    }

    private boolean filterByStatus(Lot lot, String status) {
        if (status == null || status.equals("ALL")) return true;
        String dbStatus = lot.getStatus().name();
        if (status.equals("LOT_RUN")) return dbStatus.equals("IN_PROGRESS");
        if (status.equals("LOT_OK")) return dbStatus.equals("COMPLETED");
        if (status.equals("LOT_ERR")) return dbStatus.equals("DEFECTIVE");
        return true;
    }
}