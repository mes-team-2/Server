package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.MaterialLotStatus;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.request.MaterialRegisterDto;
import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.dto.response.MaterialInventoryResponseDto;
import com.final_project.battery.dto.response.MaterialLotResponseDto;
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
    private final MaterialLotRepository materialLotRepository;

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
    public void consumeMaterialForLot(Lot lot) {
        // 1. 해당 제품(Lot)을 만드는 데 필요한 BOM 조회
        List<BOM> boms = bomRepository.findByProduct(lot.getProduct());

        for (BOM bom : boms) {
            // 필요 수량 계산 (지시수량 * BOM 소요량)
            BigDecimal requiredQty = bom.getRequiredQty().multiply(new BigDecimal(lot.getLotQty()));

            // 2. FIFO 로직: 가장 오래된 자재 Lot부터 가져옴
            List<MaterialLot> availableLots = materialLotRepository.findAvailableLotsByMaterial(bom.getMaterial());

            BigDecimal remainingNeed = requiredQty;

            for (MaterialLot matLot : availableLots) {
                if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) break;

                // 이 Lot에서 뺄 수 있는 양 계산
                BigDecimal currentStock = matLot.getRemainQty();
                BigDecimal deductQty = currentStock.min(remainingNeed); // 둘 중 작은 값

                // 재고 차감
                matLot.setRemainQty(currentStock.subtract(deductQty));
                if (matLot.getRemainQty().compareTo(BigDecimal.ZERO) == 0) {
                    matLot.setStatus(MaterialLotStatus.EXHAUSTED); // 다 썼으면 상태 변경
                }

                // 트랜잭션 기록 (어떤 자재 Lot를 썼는지 명시)
                MaterialTx tx = MaterialTx.builder()
                        .txType(TxType.CONSUME)
                        .material(bom.getMaterial())
                        .materialLot(matLot) // [중요] 추적성 확보
                        .lot(lot)
                        .workOrder(lot.getWorkOrder())
                        .qty(deductQty)
                        .build();
                materialTxRepository.save(tx);

                remainingNeed = remainingNeed.subtract(deductQty);
            }

            // 만약 모든 Lot을 뒤졌는데도 부족하다면? -> 에러 처리 or 마이너스 재고 (여기선 에러)
            if (remainingNeed.compareTo(BigDecimal.ZERO) > 0) {
                throw new RuntimeException("자재 재고 부족: " + bom.getMaterial().getMaterialName());
            }
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
        // Repository의 JPQL을 호출하여 한 방에 DTO로 가져옵니다.
        return materialRepository.findAllWithStock();
    }

    // 4. [수정] 신규 자재 등록 및 자재 Lot 생성
    @Transactional
    public void registerMaterial(MaterialRegisterDto dto) {
        // 자재 마스터 생성
        String autoCode = "MAT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Material material = Material.builder()
                .materialCode(autoCode)
                .materialName(dto.getMaterialName())
                .unit(dto.getUnit().toUpperCase())
                .createdAt(LocalDateTime.now())
                .build();
        materialRepository.save(material);

        // 자재 Lot 생성 (초기 재고)
        if (dto.getInitialStock() != null && dto.getInitialStock().compareTo(BigDecimal.ZERO) > 0) {
            String lotNo = "ML-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));

            MaterialLot materialLot = MaterialLot.builder()
                    .material(material)
                    .materialLotNo(lotNo)
                    .inQty(dto.getInitialStock())
                    .remainQty(dto.getInitialStock())
                    .status(MaterialLotStatus.AVAILABLE)
                    .build();
            materialLotRepository.save(materialLot);

            // 입고 이력 기록
            MaterialTx tx = MaterialTx.builder()
                    .txType(TxType.INBOUND)
                    .material(material)
                    .materialLot(materialLot)
                    .qty(dto.getInitialStock())
                    .build();
            materialTxRepository.save(tx);
        }
    }

    // 특정 자재의 Lot 목록 조회 (상세 팝업용)
    @Transactional(readOnly = true)
    public List<MaterialLotResponseDto> getMaterialLots(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("자재를 찾을 수 없습니다."));

        // Repository에서 해당 자재의 모든 Lot을 가져와서 DTO로 변환
        return materialLotRepository.findByMaterialOrderByInputDateDesc(material).stream()
                .map(MaterialLotResponseDto::from)
                .collect(Collectors.toList());
    }
}
