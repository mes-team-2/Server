package com.final_project.battery.controller;

import com.final_project.battery.dto.request.ProductCreateDto;
import com.final_project.battery.dto.request.ProductUpdateDto;
import com.final_project.battery.dto.response.ProductResponseDto;
import com.final_project.battery.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getProductList() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateDto dto) {
        productService.createProduct(dto);
        return ResponseEntity.ok("제품이 등록되었습니다.");
    }

    @PutMapping("/{productId}")
    public ResponseEntity<String> updateProduct(@PathVariable Long productId, @RequestBody ProductUpdateDto dto) {
        productService.updateProduct(productId, dto);
        return ResponseEntity.ok("제품 정보가 수정되었습니다.");
    }
}