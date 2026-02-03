package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.dto.response.WorkOrderDetailDto;
import com.final_project.battery.dto.response.WorkOrderResponseDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;
    private final ProcessLogRepository processLogRepository;
    private final MaterialTxRepository materialTxRepository;
    private final WorkerRepository workerRepository;

    @Transactional
    public Long createWorkOrder(WorkOrderCreateDto dto) {
        // 1. 제품 조회
        Product product = productRepository.findByProductCode(dto.getProductCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 제품 코드입니다."));

        Worker manager = workerRepository.findByWorkerCode(dto.getWorkerCode())
                .orElseThrow(() -> new RuntimeException("작업자 정보 없음"));

        // 2. 작업지시 번호 생성 (WO-yyyyMMdd-XXXX)
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = workOrderRepository.countByCreatedAtBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        String woNo = String.format("WO-%s-%03d", today, count + 1);

        LocalDateTime dueDate = LocalDate.now().plusDays(3).atStartOfDay(); // 기본값
        if (dto.getDueDate() != null && !dto.getDueDate().isEmpty()) {
            dueDate = LocalDate.parse(dto.getDueDate()).atStartOfDay();
        }

        // 3. 작업지시 저장
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo(woNo);
        workOrder.setProduct(product);
        workOrder.setPlannedQty(dto.getPlannedQty());
        workOrder.setStartedAt(LocalDateTime.now());
        workOrder.setDueDate(dueDate);
        workOrder.setManager(manager);
        workOrder.setEndedAt(null);
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setCreatedAt(LocalDateTime.now());

        workOrderRepository.save(workOrder);

        // 4. Batch Lot 자동 생성
        String lotNo = String.format("LOT-%s-%03d", today, count + 1);

        Lot lot = Lot.builder()
                .lotNo(lotNo)
                .product(product)
                .workOrder(workOrder)
                .lotQty(dto.getPlannedQty())
                .status(LotStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        lotRepository.save(lot);

        log.info("✅ 신규 작업지시 생성 완료: {} (제품: {})", woNo, product.getProductName());
        return null;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponseDto> getWorkOrderList() {
        // 최신순(내림차순) 정렬
        List<WorkOrder> list = workOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return list.stream()
                .map(WorkOrderResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkOrderDetailDto getWorkOrderDetail(String workOrderNo) {
        // 1. 작업지시 조회
        WorkOrder wo = workOrderRepository.findByWorkOrderNo(workOrderNo)
                .orElseThrow(() -> new RuntimeException("작업지시를 찾을 수 없습니다: " + workOrderNo));

        // 2. Lot 조회
        Lot lot = lotRepository.findFirstByWorkOrder(wo).orElse(null);

        // 3. 기본 정보 매핑
        WorkOrderDetailDto dto = new WorkOrderDetailDto();
        dto.setWorkOrderNo(wo.getWorkOrderNo());
        dto.setProductName(wo.getProduct().getProductName());
        dto.setPlannedQty(wo.getPlannedQty());
        dto.setStatus(wo.getStatus().name());

        if (lot == null) {
            dto.setLotInfo(null);
            dto.setProcessList(Collections.emptyList());
            dto.setMaterialList(Collections.emptyList());
            return dto;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 4. Lot 정보 매핑
        WorkOrderDetailDto.LotInfoDto lotDto = new WorkOrderDetailDto.LotInfoDto();
        lotDto.setLotNo(lot.getLotNo());
        lotDto.setQty(lot.getLotQty());
        lotDto.setStatus(lot.getStatus().name());
        lotDto.setCreatedAt(lot.getCreatedAt().format(fmt));
        dto.setLotInfo(lotDto);

        // =========================================================
        // [핵심 1] 공정 이력 그룹화 (같은 공정은 1줄로 요약)
        // =========================================================
        List<ProcessLog> pLogs = processLogRepository.findByLotOrderByStartTimeAsc(lot);

        // 공정 단계별로 로그를 묶음 (LinkedHashMap으로 순서 보장)
        Map<String, List<ProcessLog>> groupedProcess = pLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getProcessStep().getStepName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<WorkOrderDetailDto.ProcessProgressDto> pList = new ArrayList<>();

        for (Map.Entry<String, List<ProcessLog>> entry : groupedProcess.entrySet()) {
            List<ProcessLog> logs = entry.getValue();
            ProcessLog firstLog = logs.get(0); // 시작 시점용
            ProcessLog lastLog = logs.get(logs.size() - 1); // 종료 시점용

            WorkOrderDetailDto.ProcessProgressDto pDto = new WorkOrderDetailDto.ProcessProgressDto();
            pDto.setId(firstLog.getProcessLogId()); // 대표 ID
            pDto.setStepName(entry.getKey());       // 공정명 (예: 전극공정)

            // 설비명 (가장 최근 설비 기준)
            pDto.setMachineName(lastLog.getMachine() != null ? lastLog.getMachine().getMachineName() : "-");

            // 상태 (하나라도 진행 중이면 RUN, 다 끝났으면 DONE -> 여기선 마지막 로그 기준)
            pDto.setStatus(lastLog.getStatus().name());

            // 시작 시간 (가장 빠른 시간)
            pDto.setStartedAt(firstLog.getStartTime() != null ? firstLog.getStartTime().format(fmt) : "-");

            // 종료 시간 (가장 늦은 시간)
            pDto.setEndedAt(lastLog.getEndTime() != null ? lastLog.getEndTime().format(fmt) : "-");

            pList.add(pDto);
        }
        dto.setProcessList(pList);


        // =========================================================
        // [핵심 2] 자재 투입 이력 그룹화 (같은 자재는 수량 합산)
        // =========================================================
        List<MaterialTx> mLogs = materialTxRepository.findByLotAndTxType(lot, TxType.CONSUME);

        // 자재명 기준으로 묶음
        Map<String, List<MaterialTx>> groupedMaterial = mLogs.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getMaterial().getMaterialName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<WorkOrderDetailDto.MaterialInputDto> mList = new ArrayList<>();

        for (Map.Entry<String, List<MaterialTx>> entry : groupedMaterial.entrySet()) {
            List<MaterialTx> txs = entry.getValue();
            MaterialTx firstTx = txs.get(0);

            // 수량 합산 (BigDecimal 사용)
            BigDecimal totalQty = txs.stream()
                    .map(MaterialTx::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            WorkOrderDetailDto.MaterialInputDto mDto = new WorkOrderDetailDto.MaterialInputDto();
            mDto.setId(Long.valueOf(firstTx.getMaterialTxId())); // 대표 ID
            mDto.setMaterialName(entry.getKey()); // 자재명 (예: 납)

            // 소수점 정리 (예: 9.00 -> 9)
            mDto.setQty(totalQty.stripTrailingZeros().toPlainString());

            mDto.setUnit(firstTx.getMaterial().getUnit()); // 단위

            // 투입 시간 (최초 투입 시간)
            mDto.setTime(firstTx.getTxTime().format(fmt));

            mList.add(mDto);
        }
        dto.setMaterialList(mList);

        return dto;
    }
}