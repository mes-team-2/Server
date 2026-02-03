package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.MaterialLotStatus;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.request.MaterialInboundDto;
import com.final_project.battery.dto.request.MaterialRegisterDto;
import com.final_project.battery.dto.response.*;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final FgInventoryRepository fgInventoryRepository;
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

    @Transactional
    public void registerMaterial(MaterialRegisterDto dto) {
        // 1. 자재 코드 자동 생성 (MAT-yyyyMMdd-HHmmss)
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String autoCode = "MAT-" + dateStr;

        // 2. 자재 마스터 생성
        Material material = Material.builder()
                .materialCode(autoCode)
                .materialName(dto.getMaterialName())
                .unit(dto.getUnit().toUpperCase()) // 대문자 통일
                .safeQty(dto.getSafeQty() != null ? dto.getSafeQty() : 0) // null이면 0
                .createdAt(LocalDateTime.now())
                .build();

        materialRepository.save(material);

        // 3. 기초 재고가 입력되었다면 -> 초기 Lot 및 입고 이력 생성
        if (dto.getInitialStock() != null && dto.getInitialStock().compareTo(BigDecimal.ZERO) > 0) {

            // Lot 번호 생성 (ML-yyMMddHHmm-INIT)
            String lotTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
            String lotNo = "ML-" + lotTimeStr + "-INIT";

            MaterialLot materialLot = MaterialLot.builder()
                    .material(material)
                    .materialLotNo(lotNo)
                    .inQty(dto.getInitialStock())
                    .remainQty(dto.getInitialStock())
                    .status(MaterialLotStatus.AVAILABLE)
                    .inputDate(LocalDateTime.now())
                    .build();

            materialLotRepository.save(materialLot);

            // 입고 트랜잭션 기록
            MaterialTx tx = MaterialTx.builder()
                    .txType(TxType.INBOUND)
                    .material(material)
                    .materialLot(materialLot)
                    .qty(dto.getInitialStock())
                    .txTime(LocalDateTime.now())
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

    @Transactional
    public void inboundMaterial(MaterialInboundDto dto) {
        // 1. 자재 조회
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 자재입니다."));

        // 2. 새로운 LOT 번호 생성 (규칙: ML-yyMMddHHmm-RANDOM)
        // 실제 현장에서는 바코드를 스캔하지만, 여기선 자동 생성
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        // 겹치지 않게 뒤에 난수나 시퀀스를 붙임 (여기선 간단히 시간 기반 + 자재ID)
        String lotNo = "ML-" + timeStr + "-" + material.getMaterialId();

        // 3. MaterialLot 생성 (재고 증가)
        MaterialLot newLot = MaterialLot.builder()
                .material(material)
                .materialLotNo(lotNo)
                .inQty(dto.getQuantity())
                .remainQty(dto.getQuantity()) // 입고 시엔 잔량 = 입고량
                .status(MaterialLotStatus.AVAILABLE)
                .inputDate(LocalDateTime.now())
                .build();

        materialLotRepository.save(newLot);

        // 4. 트랜잭션(Tx) 이력 기록 (입고 유형)
        MaterialTx tx = MaterialTx.builder()
                .txType(TxType.INBOUND)
                .material(material)
                .materialLot(newLot)
                .qty(dto.getQuantity())
                .txTime(LocalDateTime.now())
                .build();

        materialTxRepository.save(tx);
    }

    // 자재 이력 조회 페이지 가져오기
    public Page<MaterialTxResponseDto> search(
            String type,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        TxType txType = null;
        if (type != null && !type.isBlank()) {
            txType = TxType.valueOf(type); // "INBOUND", "CONSUME"
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) {
            start = startDate.atStartOfDay();
        }
        if (endDate != null) {
            end = endDate.atTime(23, 59, 59);
        }

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return materialTxRepository.search(
                txType,
                keyword,
                start,
                end,
                pageable
        );
    }
    // 자재 이력 합계
    public MaterialTxAllResponseDto getMaterialTxSummary(
            TxType type,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) {
            start = startDate.atStartOfDay(); // 00:00:00
        }

        if (endDate != null) {
            end = endDate.atTime(23, 59, 59); // 하루 끝
        }

        // 1. DB에서 합계만 가져옴 (rate는 0 상태)
        MaterialTxAllResponseDto raw =
                materialTxRepository.getSummary(type, keyword, start, end);

        BigDecimal inQty = raw.getInQty();
        BigDecimal outQty = raw.getOutUseQty();

        // null 방어
        if (inQty == null) inQty = BigDecimal.ZERO;
        if (outQty == null) outQty = BigDecimal.ZERO;

        // 2. rate 계산
        BigDecimal rate = BigDecimal.ZERO;

        if (inQty.compareTo(BigDecimal.ZERO) > 0) {
            rate = outQty
                    .divide(inQty, 4, RoundingMode.HALF_UP) // 소수 4자리 계산
                    .multiply(BigDecimal.valueOf(100))      // %
                    .setScale(1, RoundingMode.HALF_UP);     // 소수 1자리
        }

        // 3. 새 DTO 만들어 반환
        return new MaterialTxAllResponseDto(
                inQty,
                outQty,
                rate
        );
    }

    // 자재 이력 상세 dto
    public MaterialTxDetailResponseDto getDetail(Integer id) {

        MaterialTxDetailResponseDto q =
                materialTxRepository.findDetail(id);

        BigDecimal afterQty = q.getRemainQty();
        BigDecimal qty = q.getQty();

        BigDecimal beforeQty;

        if (q.getTxType() == TxType.INBOUND) {
            beforeQty = afterQty.subtract(qty);
        } else {
            beforeQty = afterQty.add(qty);
        }

        // 자재 마이너스 값 안되게
        if (beforeQty.compareTo(BigDecimal.ZERO) < 0) {
            beforeQty = BigDecimal.ZERO;
        }

        // 임시값 덮어쓰기
        q.setBeforeQty(beforeQty);

        // Response DTO로 변환
        return MaterialTxDetailResponseDto.builder()
                .id(q.getId())
                .txType(q.getTxType())
                .txTime(q.getTxTime())
                .qty(q.getQty())
                .beforeQty(q.getBeforeQty())
                .remainQty(afterQty)
                .materialCode(q.getMaterialCode())
                .materialName(q.getMaterialName())
                .build();
    }


    // LOT 목록 조회
    public Page<MaterialLotManagementResponseDto> searchLot(
            String status,      // 프론트에서 String으로 받음
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        MaterialLotStatus lotStatus = null;

        // status 문자열 → ENUM 변환
        if (status != null && !status.isBlank()) {
            lotStatus = MaterialLotStatus.valueOf(status);
        }

        // 날짜 변환
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) {
            start = startDate.atStartOfDay();
        }

        if (endDate != null) {
            end = endDate.atTime(23, 59, 59);
        }

        // keyword 공백 처리
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return materialLotRepository.searchLot(
                lotStatus,
                keyword,
                start,
                end,
                pageable
        );
    }

    // LOT 합계 조회 (카드)
    public MaterialLotAllResponseDto getSummary(
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {

        MaterialLotStatus lotStatus = null;

        if (status != null && !status.isBlank()) {
            lotStatus = MaterialLotStatus.valueOf(status);
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) {
            start = startDate.atStartOfDay();
        }

        if (endDate != null) {
            end = endDate.atTime(23, 59, 59);
        }

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return materialLotRepository.getSummary(
                lotStatus,
                keyword,
                start,
                end
        );
    }

}
