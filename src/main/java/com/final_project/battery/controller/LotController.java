package com.final_project.battery.controller;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotRepository lotRepository;

    // 현재 생산 진행 중인(IN_PROGRESS) Lot 목록만 조회
    // C# 시뮬레이터가 "작업할 Lot 선택" 할 때 사용
    @GetMapping("/active")
    public ResponseEntity<List<Lot>> getActiveLots() {
        return ResponseEntity.ok(lotRepository.findByStatus(LotStatus.IN_PROGRESS));
    }
}
