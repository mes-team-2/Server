package com.final_project.battery.service;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.QualityTestResult;
import com.final_project.battery.dto.request.ProductionLogRequestDto;
import com.final_project.battery.repository.*;
import com.final_project.battery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final ProductionLogRepository productionLogRepository;
    private final MachineRepository machineRepository;
    private final LotRepository lotRepository;
    private final ProcessStepRepository processStepRepository;
    private final WorkerRepository workerRepository;
    private final DefectLogRepository defectLogRepository;
    private final QualityTestRepository qualityTestRepository;
    private final InventoryService inventoryService; // 자재 소모 로직 호출용

//    @Transactional
//    public void saveProductionLog(ProductionLogRequestDto dto) {
//        // 1. 작업자 식별 (JWT 토큰에서 추출하거나, 없으면 기본값 사용)
//        String workerCode = SecurityUtil.getCurrentWorkerCode();
//        if (workerCode == null) workerCode = "WORKER-001"; // C# 테스트용 기본값
//
//        Worker worker = workerRepository.findByWorkerCode(workerCode)
//                .orElseThrow(() -> new RuntimeException("Worker not found"));
//
//        // 2. 기본 정보 조회
//        Machine machine = machineRepository.findById(dto.getMachineId())
//                .orElseThrow(() -> new RuntimeException("Machine not found"));
//
//        Lot lot = lotRepository.findById(dto.getLotId())
//                .orElseThrow(() -> new RuntimeException("Lot not found"));
//
//        ProcessStep processStep = processStepRepository.findById(dto.getProcessStepId())
//                .orElseThrow(() -> new RuntimeException("ProcessStep not found"));
//
//        // [핵심 추가] 3. 자재 소모 (FIFO) - 첫 번째 공정(전극 공정)일 때만 자재를 깐다!
//        // (만약 모든 공정마다 자재가 들어간다면 로직 변경 필요하지만, 보통 전극에서 양극재/음극재 투입됨)
//        if (processStep.getSeq() == 1) {
//            // 이미 투입된 Lot인지 체크하는 로직이 있으면 좋지만,
//            // 일단 C#이 공정별로 한 번씩만 데이터를 보낸다고 가정하고 소모시킴
//            // 주의: C#에서 센서 데이터를 1초마다 보내면 자재가 순식간에 다 털릴 수 있음.
//            // 해결책: "생산 실적(goodQty)"이 올라갈 때만 자재를 까야 함.
//
//            // 여기서는 심플하게: C#이 "공정 완료 시점"에만 이 API를 호출한다고 가정하거나,
//            // 아니면 C# 로직에서 "1개 생산될 때마다" 호출한다고 가정.
//            // 우리는 "1개 생산" -> "BOM 만큼 자재 소모"로 연결.
//            try {
//                inventoryService.consumeMaterialForLot(lot); // 여기서 자재 차감 발생
//            } catch (Exception e) {
//                // 자재 부족해도 일단 로그는 남기되, 경고 출력
//                System.out.println("⚠️ 자재 소모 실패: " + e.getMessage());
//            }
//        }
//
//        // 4. 생산 로그 저장
//        ProductionLog log = new ProductionLog();
//        log.setWorkOrder(lot.getWorkOrder());
//        log.setLot(lot);
//        log.setMachine(machine);
//        log.setProcessStep(processStep);
//        log.setWorker(worker);
//        log.setGoodQty(dto.getGoodQty());
//        log.setBadQty(dto.getBadQty());
//        log.setTemperature(dto.getTemperature());
//        log.setVoltage(dto.getVoltage());
//        log.setStartedAt(LocalDateTime.now().minusSeconds(10)); // 임의 시간
//        log.setEndedAt(LocalDateTime.now());
//
//        productionLogRepository.save(log);
//
//        // 5. 품질 검사 결과 저장 (자동 판정)
//        QualityTest qualityTest = new QualityTest();
//        qualityTest.setLot(lot);
//        qualityTest.setMachine(machine);
//        qualityTest.setWorker(worker);
//        qualityTest.setTestedAt(LocalDateTime.now());
//        qualityTest.setResult(dto.getBadQty() > 0 ? QualityTestResult.FAIL : QualityTestResult.PASS);
//        qualityTestRepository.save(qualityTest);
//
//        // 6. 불량 상세 기록
//        if (dto.getBadQty() > 0) {
//            DefectLog defectLog = new DefectLog();
//            defectLog.setProductionLog(log);
//            defectLog.setLot(lot);
//            defectLog.setMachine(machine);
//            defectLog.setWorker(worker);
//            defectLog.setDefectType("AUTO_DETECT");
//            defectLog.setDefectQty(dto.getBadQty());
//            defectLogRepository.save(defectLog);
//        }
//
//        // 7. [핵심] 마지막 공정(5번 검사) 통과 시 -> 완제품 입고 처리
//        if (processStep.getSeq() == 5 && dto.getGoodQty() > 0) {
//            inventoryService.addStockFromProduction(lot, dto.getGoodQty());
//            System.out.println("🎉 완제품 입고 완료: Lot " + lot.getLotNo() + ", 수량 " + dto.getGoodQty());
//        }
//    }
}