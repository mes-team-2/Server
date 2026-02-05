package com.final_project.battery.controller;

import com.final_project.battery.dto.response.ProductLotDetailDto;
import com.final_project.battery.dto.response.ProductLotResponseDto;
import com.final_project.battery.service.ProductLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-lots")
@RequiredArgsConstructor
public class ProductLotController {

    private final ProductLotService productLotService;

    @GetMapping
    public ResponseEntity<List<ProductLotResponseDto>> searchLots(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(productLotService.searchLots(keyword, startDate, endDate, status));
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<ProductLotDetailDto> getLotDetail(@PathVariable Long lotId) {
        return ResponseEntity.ok(productLotService.getLotDetail(lotId));
    }
}