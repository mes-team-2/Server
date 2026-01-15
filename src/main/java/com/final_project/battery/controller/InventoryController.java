package com.final_project.battery.controller;

import com.final_project.battery.dto.response.FgInventoryResponseDto;
import com.final_project.battery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<FgInventoryResponseDto>> getFgInventoryList() {
        return ResponseEntity.ok(inventoryService.getAllFgInventory());
    }
}
