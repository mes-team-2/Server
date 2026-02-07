package com.final_project.battery.controller;

import com.final_project.battery.dto.request.ShipmentCreateRequestDto;
import com.final_project.battery.dto.response.ShipmentResponseDto;
import com.final_project.battery.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor

public class ShipmentController {
    private final ShipmentService shipmentService;

    @PostMapping
    public void create(@RequestBody ShipmentCreateRequestDto req) {
        shipmentService.createShipment(req);
    }

    @GetMapping
    public List<ShipmentResponseDto> list(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end
    ) {
        return shipmentService.getShipmentHistory(start, end);
    }
}
