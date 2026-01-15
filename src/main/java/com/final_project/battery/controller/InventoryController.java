package com.final_project.battery.controller;

import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // React에서 호출
    @GetMapping
    public ResponseEntity<List<FgInventoryResponseDto>> getInventoryList() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
}
