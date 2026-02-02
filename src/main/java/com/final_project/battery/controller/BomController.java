package com.final_project.battery.controller;

import com.final_project.battery.dto.request.BomUpdateRequestDto;
import com.final_project.battery.dto.response.BomResponseDto;
import com.final_project.battery.service.BomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    // 제품별 BOM 조회
    @GetMapping("/{productCode}")
    public ResponseEntity<List<BomResponseDto>> getBomList(@PathVariable String productCode) {
        return ResponseEntity.ok(bomService.getBomListByProduct(productCode));
    }

    // BOM 수정
    @PutMapping("/{bomId}")
    public ResponseEntity<String> updateBom(@PathVariable Long bomId, @RequestBody BomUpdateRequestDto dto) {
        bomService.updateBom(bomId, dto);
        return ResponseEntity.ok("BOM 정보가 수정되었습니다.");
    }
}