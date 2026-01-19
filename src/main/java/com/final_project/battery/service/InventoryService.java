package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.request.MaterialRegisterDto;
import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.dto.response.MaterialInventoryResponseDto;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final FgInventoryRepository fgInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final MaterialTxRepository materialTxRepository;
    private final BomRepository bomRepository;
    private final MaterialRepository materialRepository;

    // 1. 완제품 입고 (검사 공정 통과 시 호출)
    @Transactional
    public void addStockFromProduction(Lot lot, int goodQty) {
        if (goodQty <= 0) return;

        // Lot 기준으로 재고가 이미 있는지 확인 (없을 시 생성)
        FgInventory inventory = fgInventoryRepository.findByLot(lot)
                .orElse(null);

        if (inventory == null) {
            inventory = FgInventory.builder()
                    .product(lot.getProduct())
                    .lot(lot)
                    .stockQty(goodQty)
                    .locationCode("ZZ-FG-01") // 지존 완제품 창고 (창고 하나인 세계관)
                    .build();
            fgInventoryRepository.save(inventory);
        } else {
            // 이미 있으면 기존 수량에서 양품만큼 추가
            inventory.setStockQty(inventory.getStockQty() + goodQty);
        }
    }

    // 2. 자재 소모 (BOM 기반 자동 차감)
    @Transactional
    public void consumeMaterialForProduction(Lot lot, int productionQty) {
        // 생산수량(양품 + 불량) 만큼 자재 소모
        Product product = lot.getProduct();
        List<BOM> boms = bomRepository.findByProduct(product);

        for (BOM bom : boms) {
            // 소요량 = BOM설정량 * 생산수량
            BigDecimal requiredQty = bom.getRequiredQty()
                    .multiply(new BigDecimal(productionQty));

            Inventory inventory = inventoryRepository.findByMaterial(bom.getMaterial())
                    .orElseThrow(() -> new RuntimeException("자재 재고 정보 없음: " + bom.getMaterial().getMaterialName()));

            // 재고 부족 체크
            if (inventory.getStockQty().compareTo(requiredQty) < 0) {
                throw new RuntimeException("자재 재고 부족 (" + bom.getMaterial().getMaterialName() + ")");
            }

            // 재고 차감
            inventory.setStockQty(inventory.getStockQty().subtract(requiredQty));

            // 자재 입고 출고 이력(Tx) 저장
            MaterialTx tx = new MaterialTx();
            tx.setMaterial(bom.getMaterial());
            tx.setLot(lot);
            tx.setWorkOrder(lot.getWorkOrder());
            tx.setTxType(TxType.CONSUME);
            tx.setQty(requiredQty);

            materialTxRepository.save(tx);
        }
    }

    // 완제품 재고 조회 (React)
    @Transactional(readOnly = true)
    public List<FgInventoryResponseDto> getAllFgInventory() {
        return fgInventoryRepository.findAll().stream()
                .map(FgInventoryResponseDto::from)
                .collect(Collectors.toList());
    }

    // 자재 재고 현황 조회 (React)
    @Transactional(readOnly = true)
    public List<MaterialInventoryResponseDto> getMaterialInventory() {
        return materialRepository.findAllWithStock();
    }

    // 신규 자재 등록 및 기초 재고 설정
    @Transactional
    public void registerMaterial(MaterialRegisterDto dto) {
        String autoCode = "MAT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        Material material = Material.builder()
                .materialCode(autoCode)
                .materialName(dto.getMaterialName())
                .unit(dto.getUnit().toUpperCase())
                .createdAt(LocalDateTime.now())
                .build();

        materialRepository.save(material);

        Inventory inventory = new Inventory();
        inventory.setMaterial(material);
        inventory.setStockQty(dto.getInitialStock() != null ? dto.getInitialStock() : BigDecimal.ZERO);
        inventory.setUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);
    }
}
