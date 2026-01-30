package com.final_project.battery.controller;

import com.final_project.battery.dto.request.MaterialInboundDto;
import com.final_project.battery.dto.request.MaterialRegisterDto;
import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.dto.response.MaterialInventoryResponseDto;
import com.final_project.battery.dto.response.MaterialLotResponseDto;
import com.final_project.battery.dto.response.MaterialTxResponseDto;
import com.final_project.battery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
