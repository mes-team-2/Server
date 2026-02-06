package com.final_project.battery.service;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.dto.response.ProductLotDetailDto;
import com.final_project.battery.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductLotQrService {

    private final LotRepository lotRepository;
    private final ProductLotService productLotService; // 기존 서비스 주입 (로직 재활용)

    @Transactional(readOnly = true)
    public ProductLotDetailDto getLotDetailByNo(String lotNo) {
        // 1. Lot 번호로 Lot 엔티티 조회
        Lot lot = lotRepository.findByLotNo(lotNo)
                .orElseThrow(() -> new RuntimeException("해당 LOT 번호를 찾을 수 없습니다: " + lotNo));

        // 2. ID를 추출하여 기존의 상세 조회 로직(공정/자재 집계)을 그대로 수행
        // (새로 로직을 짜면 유지보수 시 두 곳을 수정해야 하므로 위임하는 것이 좋습니다)
        return productLotService.getLotDetail(lot.getLotId());
    }
}