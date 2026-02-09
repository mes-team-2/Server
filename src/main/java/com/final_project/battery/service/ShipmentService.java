package com.final_project.battery.service;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.Shipment;
import com.final_project.battery.domain.common.ShipmentType;
import com.final_project.battery.dto.request.ShipmentCreateRequestDto;
import com.final_project.battery.dto.response.ShipmentResponseDto;
import com.final_project.battery.repository.FgInventoryRepository;
import com.final_project.battery.repository.LotRepository;
import com.final_project.battery.repository.ShipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final FgInventoryRepository fgInventoryRepository;
    private final LotRepository lotRepository;

    // ======================
    // 출하 등록
    // ======================
    @Transactional
    public void createShipment(ShipmentCreateRequestDto req) {
        System.out.println(">>> req = " + req.getQty());
        System.out.println(">>> req = " + req.getTxType());
        System.out.println(">>> req = " + req.getUnit());
        System.out.println(">>> req = " + req.getProductName());
        System.out.println(">>> req = " + req.getProductCode());
        System.out.println(">>> req = " + req.getQty());
        System.out.println(">>> req.lotNo = [" + req.getLotNo() + "]");
//        req.setUnit("EA");
//        req.setStatusKey("out");
//        req.setTxTime(LocalDateTime.now());
//        req.setTxType(ShipmentType.SHIPMENT_OUT);


        String lotNo = req.getLotNo();
        System.out.println(">>> searching lotNo = [" + lotNo + "]");

        // 1️⃣ LOT 조회
        Lot lot = lotRepository.findByLotNo(req.getLotNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 LOT 없음"));


        log.info(">>> ShipmentCreateRequestDto = {}", req);
        log.info(">>> lotNo from request = [{}]", req.getLotNo());

        // 2️⃣ LOT 기준 재고 조회
        FgInventory inventory = fgInventoryRepository.findByLot(lot)
                .orElseThrow(() -> new IllegalArgumentException("LOT 재고 없음"));

        // 3️⃣ 재고 차감
        if (inventory.getStockQty() < req.getQty()) {
            throw new IllegalArgumentException("출하 수량이 재고보다 많습니다.");
        }
        inventory.setStockQty(inventory.getStockQty() - req.getQty());

        // 4️⃣ Shipment 저장
        Shipment shipment = Shipment.builder()
                .productCode(lot.getProduct().getProductCode())
                .productName(lot.getProduct().getProductName())
                .productLotNo(lot.getLotNo())
                .txType(req.getTxType())
                .txTime(req.getTxTime())
                .statusKey(req.getStatusKey())
                .qty(req.getQty())
                .unit(req.getUnit())
                .location(req.getLocation())
                .build();

        shipmentRepository.save(shipment);
    }

    // ======================
    // 출하 이력 조회
    // ======================
    @Transactional
    public List<ShipmentResponseDto> getShipmentHistory(
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<Shipment> shipments;

        if (start == null || end == null) {
            shipments = shipmentRepository.findAllByOrderByTxTimeDesc();
        } else {
            shipments = shipmentRepository
                    .findByTxTimeBetweenOrderByTxTimeDesc(start, end);
        }

        return shipments.stream()
                .map(this::toDto)
                .toList();
    }

    private ShipmentResponseDto toDto(Shipment s) {
        return ShipmentResponseDto.builder()
                .id(s.getId())
                .txTime(s.getTxTime())
                .txType(s.getTxType().name())
                .status_key(s.getStatusKey())
                .productCode(s.getProductCode())
                .productLotNo(s.getProductLotNo())
                .productName(s.getProductName())
                .qty(s.getQty())
                .unit(s.getUnit())
                .location(s.getLocation())
                .build();
    }
}
