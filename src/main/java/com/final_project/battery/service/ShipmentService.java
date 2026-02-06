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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final FgInventoryRepository fgInventoryRepository;

    // ======================
    // 출하 등록
    // ======================
    @Transactional
    public void createShipment(ShipmentCreateRequestDto req) {

        // 1️⃣ 재고 조회
        FgInventory inventory = fgInventoryRepository
                .findByProduct_ProductCode(req.getProductCode())
                .orElseThrow(() -> new IllegalArgumentException("재고 없음"));

        // 2️⃣ 재고 차감
        if (inventory.getStockQty() < req.getQty()) {
            throw new IllegalArgumentException("출하 수량이 재고보다 많습니다.");
        }

        inventory.setStockQty(
                inventory.getStockQty() - req.getQty()
        );


        // 3️⃣ 출하 이력 생성
        Shipment shipment = Shipment.builder()
                .txTime(LocalDateTime.now())
                .txType(ShipmentType.SHIPMENT_OUT)
                .statusKey("out")
                .productCode(req.getProductCode())
                .productName(req.getProductName())
                .qty(-Math.abs(req.getQty()))
                .unit(req.getUnit())
                .location(req.getLocation())
                .note(req.getNote())
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
                .tx_time(s.getTxTime())
                .tx_type(s.getTxType().name())
                .status_key(s.getStatusKey())
                .productCode(s.getProductCode())
                .productName(s.getProductName())
                .qty(s.getQty())
                .unit(s.getUnit())
                .location(s.getLocation())
                .note(s.getNote())
                .build();
    }
}
