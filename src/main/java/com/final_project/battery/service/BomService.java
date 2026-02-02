package com.final_project.battery.service;

import com.final_project.battery.domain.BOM;
import com.final_project.battery.domain.Product;
import com.final_project.battery.dto.request.BomUpdateRequestDto;
import com.final_project.battery.dto.response.BomResponseDto;
import com.final_project.battery.repository.BomRepository;
import com.final_project.battery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BomRepository bomRepository;
    private final ProductRepository productRepository;

    // 특정 제품의 BOM 목록 조회
    @Transactional(readOnly = true)
    public List<BomResponseDto> getBomListByProduct(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("제품 없음"));

        return bomRepository.findByProduct(product).stream()
                .map(BomResponseDto::new)
                .collect(Collectors.toList());
    }

    // BOM 수정 (수량, 공정)
    @Transactional
    public void updateBom(Long bomId, BomUpdateRequestDto dto) {
        BOM bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new RuntimeException("BOM 정보 없음"));

        if (dto.getQty() != null) {
            bom.setRequiredQty(BigDecimal.valueOf(dto.getQty()));
        }
        if (dto.getProcess() != null) {
            bom.setNote(dto.getProcess());
        }
        bomRepository.save(bom);
    }
}