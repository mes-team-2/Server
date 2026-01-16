package com.final_project.battery.controller;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.domain.Product;
import com.final_project.battery.repository.MachineRepository;
import com.final_project.battery.repository.ProcessStepRepository;
import com.final_project.battery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final ProductRepository productRepository;
    private final MachineRepository machineRepository;
    private final ProcessStepRepository processStepRepository;

    // 모든 제품 목록 조회
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // 모든 설비 목록 조회 (C# 시뮬 세팅 시 사용)
    @GetMapping("/machines")
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineRepository.findAll());
    }

    // 모든 공정 목록 조회
    @GetMapping("/processes")
    public ResponseEntity<List<ProcessStep>> getAllProcessStep() {
        return ResponseEntity.ok(processStepRepository.findAll());
    }
}
