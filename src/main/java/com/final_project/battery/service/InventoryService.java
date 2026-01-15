package com.final_project.battery.service;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.Lot;
import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.repository.FgInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final FgInventoryRepository fgInventoryRepository;

    // [핵심] 생산 실적에 따라 재고 추가
    @Transactional
    public void addStockFromProduction(Lot lot, int goodQty) {
        if (goodQty <= 0) return;

        FgInventory inventory = fgInventoryRepository.findByLot(lot)
                .orElse(null);

        if (inventory == null) {
            // 신규 입고
            inventory = FgInventory.builder()
                    .product(lot.getProduct())
                    .lot(lot)
                    .stockQty(goodQty)
                    .locationCode("WH-A-01") // 기본 창고 (임시)
                    .build();
            fgInventoryRepository.save(inventory);
        } else {
            // 추가 입고 (수량 증가)
            inventory.setStockQty(inventory.getStockQty() + goodQty);
        }
    }

    // React 조회용
    @Transactional(readOnly = true)
    public List<FgInventoryResponseDto> getAllInventory() {
        return fgInventoryRepository.findAll().stream()
                .map(FgInventoryResponseDto::from)
                .collect(Collectors.toList());
    }
}