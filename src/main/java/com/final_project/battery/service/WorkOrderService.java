package com.final_project.battery.service;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.Product;
import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.repository.LotRepository;
import com.final_project.battery.repository.ProductRepository;
import com.final_project.battery.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;

    @Transactional
    public Long createWorkOrder(WorkOrderCreateDto dto) {
        // 1. 제품 조회
        Product product = productRepository.findByProductCode(dto.getProductCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 제품 코드입니다."));

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
        return workOrder.getWorkOrderId();
    }
}