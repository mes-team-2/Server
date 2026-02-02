package com.final_project.battery.service;

import com.final_project.battery.domain.BOM;
import com.final_project.battery.domain.Material;
import com.final_project.battery.domain.Product;
import com.final_project.battery.dto.request.ProductCreateDto;
import com.final_project.battery.dto.request.ProductUpdateDto;
import com.final_project.battery.dto.response.ProductResponseDto;
import com.final_project.battery.repository.BomRepository;
import com.final_project.battery.repository.MaterialRepository;
import com.final_project.battery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final BomRepository bomRepository;

    // 전체 조회
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 제품 생성 (+BOM)
    @Transactional
    public void createProduct(ProductCreateDto dto) {
        ProductCreateDto.ProductInfo info = dto.getProduct();

        // 1. 제품 저장 (unit은 EA로 고정)
        Product product = Product.builder()
                .productCode(info.getProductCode())
                .productName(info.getProductName())
                .voltage(info.getVoltage())
                .capacityAh(info.getCapacityAh())
                .unit("EA") // [수정] 서비스 레벨에서 강제 주입
                .build();
        productRepository.save(product);

        // 2. BOM 저장
        if (dto.getBomItems() != null) {
            for (ProductCreateDto.BomItem item : dto.getBomItems()) {
                Material material = materialRepository.findByMaterialCode(item.getMaterialCode())
                        .orElseThrow(() -> new RuntimeException("자재 없음: " + item.getMaterialCode()));

                BOM bom = BOM.builder()
                        .product(product)
                        .material(material)
                        .requiredQty(BigDecimal.valueOf(item.getQty()))
                        .scrapRate(BigDecimal.ZERO)
                        .note("조립공정") // 기본값
                        .build();
                bomRepository.save(bom);
            }
        }
    }

    // 제품 수정
    @Transactional
    public void updateProduct(Long productId, ProductUpdateDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("제품 없음"));

        if (dto.getProductName() != null) product.setProductName(dto.getProductName());
        if (dto.getCapacityAh() != null) product.setCapacityAh(dto.getCapacityAh());

    }
}