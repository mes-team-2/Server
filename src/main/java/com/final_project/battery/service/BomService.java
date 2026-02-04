package com.final_project.battery.service;

import com.final_project.battery.domain.BOM;
import com.final_project.battery.domain.Material;
import com.final_project.battery.domain.Product;
import com.final_project.battery.dto.request.BomCreateDto;
import com.final_project.battery.dto.request.BomUpdateRequestDto;
import com.final_project.battery.dto.response.BomResponseDto;
import com.final_project.battery.repository.BomRepository;
import com.final_project.battery.repository.MaterialRepository;
import com.final_project.battery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BomRepository bomRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    // 특정 제품의 BOM 목록 조회
    @Transactional(readOnly = true)
    public List<BomResponseDto> getBomListByProduct(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("제품 없음"));

        return bomRepository.findByProduct(product).stream()
                // [New] 소요량이 0보다 큰 것만 필터링 (0이면 삭제된 것으로 간주하여 목록에서 제외)
                .filter(bom -> bom.getRequiredQty().compareTo(BigDecimal.ZERO) > 0)
                .map(BomResponseDto::new)
                .collect(Collectors.toList());
    }

    // BOM 생성
    @Transactional
    public void createBom(BomCreateDto dto) {
        Product product = productRepository.findByProductCode(dto.getProductCode())
                .orElseThrow(() -> new RuntimeException("제품 없음: " + dto.getProductCode()));

        Material material = materialRepository.findByMaterialCode(dto.getMaterialCode())
                .orElseThrow(() -> new RuntimeException("자재 없음: " + dto.getMaterialCode()));

        // 이미 존재하면 업데이트로 처리 (안전장치)
        Optional<BOM> existing = bomRepository.findByProductAndMaterial(product, material);
        if (existing.isPresent()) {
            BOM bom = existing.get();
            bom.setRequiredQty(BigDecimal.valueOf(dto.getQty()));
            bom.setNote(dto.getProcess());
            return;
        }

        BOM bom = BOM.builder()
                .product(product)
                .material(material)
                .requiredQty(BigDecimal.valueOf(dto.getQty()))
                .scrapRate(BigDecimal.ZERO)
                .note(dto.getProcess())
                .build();
        bomRepository.save(bom);
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