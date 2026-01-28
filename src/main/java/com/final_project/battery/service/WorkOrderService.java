package com.final_project.battery.service;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.Product;
import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.dto.request.LotCreateDto;
import com.final_project.battery.dto.request.WorkOrderCreateDto;
import com.final_project.battery.repository.LotRepository;
import com.final_project.battery.repository.ProductRepository;
import com.final_project.battery.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;

    // 작업 지시 생성
    @Transactional
    public String createWorkOrder(WorkOrderCreateDto dto) {
        // 제품 조회
        Product product = productRepository.findByProductCode(dto.getProductCode())
                .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다: " + dto.getProductCode()));

        // 지시 번호 생성 (WO-날짜-시간)
        String woNo = "WO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo(woNo);
        workOrder.setProduct(product);
        workOrder.setPlannedQty(dto.getPlannedQty());
        workOrder.setDueAt(dto.getDueDate().atStartOfDay());
        workOrder.setStatus(WorkOrderStatus.WAIT);
        workOrder.setCreatedAt(LocalDateTime.now());

        workOrderRepository.save(workOrder);

        return woNo;
    }

//    // LOT 발행
//    @Transactional
//    public String createLot(LotCreateDto dto) {
//        // 작업지시 조회
//        WorkOrder workOrder = workOrderRepository.findByWorkOrderNo(dto.getWorkOrderNo())
//                .orElseThrow(() -> new RuntimeException("작업 지시를 찾을 수 없습니다: " + dto.getWorkOrderNo()));
//        // Lot 번호 생성 (LOT-날짜-시간)
//        String lotNo = "LOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
//
//        Lot lot = new Lot();
//        lot.setLotNo(lotNo);
//        lot.setWorkOrder(workOrder);
//        lot.setProduct(workOrder.getProduct());
//        lot.setLotQty(dto.getLotQty());
//        lot.setStatus(LotStatus.IN_PROGRESS);
//        lot.setCreatedAt(LocalDateTime.now());
//
//        lotRepository.save(lot);
//
//        // 지시 상태 변경 (대기 -> 진행중)
//        if (workOrder.getStatus() == WorkOrderStatus.WAIT) {
//            workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
//            workOrder.setStartedAt(LocalDateTime.now());
//        }
//
//        return lotNo;
//    }
}
