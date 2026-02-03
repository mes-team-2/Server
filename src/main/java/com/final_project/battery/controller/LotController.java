package com.final_project.battery.controller;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.dto.response.LotResponseDto;
import com.final_project.battery.dto.response.MaterialLotAllResponseDto;
import com.final_project.battery.dto.response.MaterialLotManagementResponseDto;
import com.final_project.battery.repository.LotRepository;
import com.final_project.battery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotRepository lotRepository;
    private final InventoryService inventoryService;

    // 현재 생산 진행 중인(IN_PROGRESS) Lot 목록만 조회
    // C# 시뮬레이터가 "작업할 Lot 선택" 할 때 사용
    @GetMapping("/active")
    public ResponseEntity<List<LotResponseDto>> getActiveLots() {

        // 1. DB에서 Entity 조회
        List<Lot> lots = lotRepository.findByStatus(LotStatus.IN_PROGRESS);

        // 2. Entity -> DTO 변환 (이 과정에서 Proxy가 초기화되며 에러 해결)
        List<LotResponseDto> dtos = lots.stream()
                .map(LotResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{lotId}/complete")
    public ResponseEntity<String> completeLot(@PathVariable Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        // 상태 변경: IN_PROGRESS -> COMPLETE
        lot.setStatus(LotStatus.COMPLETED);
        lotRepository.save(lot);

        return ResponseEntity.ok("Lot status updated to COMPLETE");
    }


    @GetMapping("/materialLot")
    public ResponseEntity<Page<MaterialLotManagementResponseDto>> getList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @PageableDefault(size = 20, sort = "inputDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                inventoryService.searchLot(
                        status,
                        keyword,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }

    @GetMapping("/materialLot/summary")
    public ResponseEntity<MaterialLotAllResponseDto> getSummary(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                inventoryService.getSummary(
                        status,
                        keyword,
                        startDate,
                        endDate
                )
        );
    }
}
