package com.final_project.battery.controller;

import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.request.MaterialInboundDto;
import com.final_project.battery.dto.request.MaterialRegisterDto;
import com.final_project.battery.dto.response.*;
import com.final_project.battery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final InventoryService inventoryService;

    // 완제품 리스트 조회 API
    @GetMapping
    public ResponseEntity<List<FgInventoryResponseDto>> getFgInventoryList() {
        return ResponseEntity.ok(inventoryService.getAllFgInventory());
    }

    // 자재 리스트 조회 API
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialInventoryResponseDto>> getMaterialInventory() {
        return ResponseEntity.ok(inventoryService.getMaterialInventory());
    }

    // 신규 자재 등록 API
    @PostMapping("/material")
    public ResponseEntity<String> registerMaterial(@RequestBody MaterialRegisterDto dto) {
        inventoryService.registerMaterial(dto);
        return ResponseEntity.ok("자재 등록 및 기초 재고 설정 완료");
    }

    // 자재 상세(Lot) 조회 API
    @GetMapping("/materials/{materialId}/lots")
    public ResponseEntity<List<MaterialLotResponseDto>> getMaterialLots(@PathVariable Long materialId) {
        return ResponseEntity.ok(inventoryService.getMaterialLots(materialId));
    }

    // 자재 입고 등록 API
    @PostMapping("/material/inbound")
    public ResponseEntity<String> inboundMaterial(@RequestBody MaterialInboundDto dto) {
        inventoryService.inboundMaterial(dto);
        return ResponseEntity.ok("입고 처리가 완료되었습니다.");
    }

    // 자재 입출고 이력 페이지 조회 API
    @GetMapping("/materialtx")
    public ResponseEntity<Page<MaterialTxResponseDto>> getMaterialTxList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @PageableDefault(size = 20, sort = "txTime", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        log.info("type={}, keyword={}, start={}, end={}",
                type, keyword, startDate, endDate);

        return ResponseEntity.ok(
                inventoryService.search(
                        type,
                        keyword,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }

    // 자재 입출고 qty 조건부 전체
    @GetMapping("/materialtx/summary")
    public MaterialTxAllResponseDto getSummary(
            @RequestParam(required = false) TxType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return inventoryService.getMaterialTxSummary(type, keyword, startDate, endDate);
    }

    // 자재 입출고 상세
    @GetMapping("/materialtx/{id}")
    public ResponseEntity<MaterialTxDetailResponseDto> getDetail(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(inventoryService.getDetail(id));
    }


    // 완제품 재고
    @GetMapping("/fginventory")
    public List<FgInventoryManagementResponseDto> search(
            @RequestParam(required = false) String keyword,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        return inventoryService.searchFg(keyword, startDate, endDate);
    }

    // 완제품 상세 조회
    @GetMapping("/fginventory/{productCode}")
    public ResponseEntity<FgInventoryManagementDetailResponseDto> getDetail(
            @PathVariable String productCode
    ) {
        return ResponseEntity.ok(
                inventoryService.getFgDetail(productCode)
        );
    }
    // ⭐ LOT 기준 완제품 재고 조회 (Shipment용)
    @GetMapping("/fginventory/lot")
    public List<FgInventoryResponseDto> getFgLotInventory() {
        return inventoryService.getFgLotInventory();
    }

}
