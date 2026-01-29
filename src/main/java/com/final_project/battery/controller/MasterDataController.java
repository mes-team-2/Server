package com.final_project.battery.controller;

import com.final_project.battery.domain.Product;
import com.final_project.battery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final ProductRepository productRepository;

    // 제품 목록 조회 (Select 박스용)
    @GetMapping("/products")
    public ResponseEntity<List<Map<String, String>>> getProductList() {
        List<Product> products = productRepository.findAll();

        List<Map<String, String>> response = products.stream()
                .map(p -> Map.of(
                        "productCode", p.getProductCode(),
                        "productName", p.getProductName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}