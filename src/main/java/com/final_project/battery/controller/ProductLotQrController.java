package com.final_project.battery.controller;

import com.final_project.battery.dto.response.ProductLotDetailDto;
import com.final_project.battery.service.ProductLotQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr/product-lots")
@RequiredArgsConstructor
public class ProductLotQrController {

    private final ProductLotQrService productLotQrService;

    // QR 조회용: lotNo(String)로 상세 정보 조회
    @GetMapping("/{lotNo}")
    public ProductLotDetailDto getLotDetailByNo(@PathVariable String lotNo) {
        return productLotQrService.getLotDetailByNo(lotNo);
    }
}