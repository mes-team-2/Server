package com.final_project.battery.init;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.*;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WorkerRepository workerRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final MaterialLotRepository materialLotRepository;
    private final MaterialTxRepository materialTxRepository;
    private final BomRepository bomRepository;
    private final ProcessStepRepository processStepRepository;
    private final MachineRepository machineRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LotRepository lotRepository;
    private final PasswordEncoder passwordEncoder;
    private final FgInventoryRepository fgInventoryRepository; // 완제품 재고용

    // 시퀀스 관리
    private int matSeq = 1;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (workerRepository.count() > 0) {
            System.out.println("⚠️ 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("🏭 [Real Factory Mode] 공장 초기 데이터를 생성합니다...");

        // ==========================================
        // 1. 작업자 생성 (조직도 반영)
        // ==========================================
        createWorker("OP-001", "김반장", "1234", Role.OPERATOR); // 현장 반장
        createWorker("OP-002", "이작업", "1234", Role.OPERATOR); // 라인 작업자
        createWorker("QC-001", "박품질", "1234", Role.OPERATOR); // 품질 검사원
        createWorker("MGR-01", "최관리", "1234", Role.ADMIN);    // 생산 관리자

        // ==========================================
        // 2. 제품 라인업 (소/중/대)
        // ==========================================
        Product pSmall = createProduct("BAT-12V-40AH", "12V 소형 배터리 (Compact)", 40, 12, "EA");
        Product pMedium = createProduct("BAT-12V-60AH", "12V 중형 배터리 (Standard)", 60, 12, "EA");
        Product pLarge = createProduct("BAT-12V-80AH", "12V 대형 배터리 (Heavy)", 80, 12, "EA");

        // ==========================================
        // 3. 자재 & 초기 재고 (8대 자재 + FIFO용 멀티 Lot)
        // ==========================================
        // 자재 생성 (Material Master)
        Material mPosPlate = createMaterial("양극 극판 (Positive Plate)", "EA", 10000);
        Material mNegPlate = createMaterial("음극 극판 (Negative Plate)", "EA", 10000);
        Material mSeparator = createMaterial("분리막 (Separator)", "M", 50000);
        Material mElectrolyte = createMaterial("전해액 (Electrolyte)", "L", 5000);
        Material mCase = createMaterial("배터리 케이스 (Al Case)", "EA", 1000);
        Material mCapAssy = createMaterial("캡 어셈블리 (Cap Assy)", "EA", 1000);
        Material mPosTab = createMaterial("양극 탭 (Positive Tab)", "EA", 5000);
        Material mNegTab = createMaterial("음극 탭 (Negative Tab)", "EA", 5000);

        // 초기 재고 입고 (Lot 2개씩 생성: 구형 재고 -> 신규 재고)
        // 시나리오: 저번달에 들어온 재고(A)가 먼저 소진되어야 함
        createMaterialLots(mPosPlate, new BigDecimal("20000"), new BigDecimal("30000")); // 총 5만
        createMaterialLots(mNegPlate, new BigDecimal("20000"), new BigDecimal("30000"));
        createMaterialLots(mSeparator, new BigDecimal("40000"), new BigDecimal("60000"));
        createMaterialLots(mElectrolyte, new BigDecimal("8000"), new BigDecimal("12000"));
        createMaterialLots(mCase, new BigDecimal("2000"), new BigDecimal("3000"));
        createMaterialLots(mCapAssy, new BigDecimal("2000"), new BigDecimal("3000"));
        createMaterialLots(mPosTab, new BigDecimal("20000"), new BigDecimal("30000"));
        createMaterialLots(mNegTab, new BigDecimal("20000"), new BigDecimal("30000"));

        // ==========================================
        // 4. BOM (Bill of Materials) - 리얼 레시피
        // ==========================================
        // [소형 40Ah]
        createBom(pSmall, mPosPlate, 10, 0.01);
        createBom(pSmall, mNegPlate, 11, 0.01);
        createBom(pSmall, mSeparator, 22, 0.01);
        createBom(pSmall, mElectrolyte, 0.5, 0.02);
        createBom(pSmall, mCase, 1, 0.0);
        createBom(pSmall, mCapAssy, 1, 0.0);
        createBom(pSmall, mPosTab, 1, 0.0);
        createBom(pSmall, mNegTab, 1, 0.0);

        // [중형 60Ah] - 표준
        createBom(pMedium, mPosPlate, 15, 0.01);
        createBom(pMedium, mNegPlate, 16, 0.01);
        createBom(pMedium, mSeparator, 32, 0.01);
        createBom(pMedium, mElectrolyte, 0.8, 0.02);
        createBom(pMedium, mCase, 1, 0.0);
        createBom(pMedium, mCapAssy, 1, 0.0);
        createBom(pMedium, mPosTab, 1, 0.0);
        createBom(pMedium, mNegTab, 1, 0.0);

        // [대형 80Ah]
        createBom(pLarge, mPosPlate, 20, 0.01);
        createBom(pLarge, mNegPlate, 21, 0.01);
        createBom(pLarge, mSeparator, 42, 0.01);
        createBom(pLarge, mElectrolyte, 1.2, 0.02);
        createBom(pLarge, mCase, 1, 0.0);
        createBom(pLarge, mCapAssy, 1, 0.0);
        createBom(pLarge, mPosTab, 1, 0.0);
        createBom(pLarge, mNegTab, 1, 0.0);

        // ==========================================
        // 5. 공정 및 설비 (A라인 / B라인 구축)`
        // ==========================================
        ProcessStep s1 = createStep("PROC-10", "전극공정(Electrode)", 10);
        ProcessStep s2 = createStep("PROC-20", "조립공정(Assembly)", 20);
        ProcessStep s3 = createStep("PROC-30", "활성화공정(Formation)", 30);
        ProcessStep s4 = createStep("PROC-40", "팩 (Pack)", 40);
        ProcessStep s5 = createStep("PROC-50", "최종 검사 (Inspection)", 50);

        // Line A (메인 라인)
        createMachine("MAC-A-01", "Stacking #A", s1, true);
        createMachine("MAC-A-02", "Packaging #A", s2, true);
        createMachine("MAC-A-03", "Injector #A", s3, true);
        createMachine("MAC-A-04", "Cycler #A", s4, true);
        createMachine("MAC-A-05", "Inspector #A", s5, true);

        // Line B (서브 라인 - 일부 가동 중지 상태 시뮬레이션)
        createMachine("MAC-B-01", "Stacking #B", s1, true);
        createMachine("MAC-B-02", "Packaging #B", s2, false); // 고장/대기 상황
        createMachine("MAC-B-03", "Injector #B", s3, true);
        createMachine("MAC-B-04", "Cycler #B", s4, true);
        createMachine("MAC-B-05", "Inspector #B", s5, true);

        // ==========================================
        // 6. 생산 이력 시뮬레이션 (과거/현재/미래)
        // ==========================================

        // Case 1: [완료] 지난주 생산 완료된 작업 (소형 100개) -> 완제품 재고로 잡힘
        createHistoryWorkOrder(pSmall, 100, WorkOrderStatus.DONE, 7);

        // Case 2: [진행중] 현재 생산 중인 작업 (중형 200개)
        createRunningWorkOrder(pMedium, 200);

        // Case 3: [대기] 내일 예정된 작업 (대형 50개)
        createPlannedWorkOrder(pLarge, 50, 1);

        System.out.println("🎉 [Real Factory] 모든 데이터 셋업 완료! 생산 라인이 가동될 준비가 되었습니다.");
    }

    // ==================================================================================
    // Helper Methods
    // ==================================================================================

    private void createWorker(String code, String name, String pw, Role role) {
        Worker w = new Worker();
        w.setWorkerCode(code);
        w.setWorkerName(name);
        w.setPassword(passwordEncoder.encode(pw));
        w.setRole(role);
        w.setCreatedAt(now());
        workerRepository.save(w);
    }

    private Product createProduct(String code, String name, int cap, int volt, String unit) {
        Product p = Product.builder()
                .productCode(code)
                .productName(name)
                .capacityAh(cap)
                .voltage(volt)
                .unit(unit)
                .build();
        return productRepository.save(p);
    }

    private Material createMaterial(String name, String unit, int safeQty) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String code = String.format("MAT-%s-%04d", dateStr, matSeq++);

        Material m = Material.builder()
                .materialCode(code)
                .materialName(name)
                .unit(unit)
                .safeQty(safeQty)
                .createdAt(now())
                .build();
        return materialRepository.save(m);
    }

    // Lot를 2개 생성하여 선입선출 테스트 환경 조성
    private void createMaterialLots(Material m, BigDecimal oldQty, BigDecimal newQty) {
        // 1. Old Lot (5일 전 입고)
        createSingleLot(m, oldQty, now().minusDays(5), "INIT-OLD");
        // 2. New Lot (어제 입고)
        createSingleLot(m, newQty, now().minusDays(1), "INIT-NEW");
    }

    private void createSingleLot(Material m, BigDecimal qty, LocalDateTime inputDate, String suffix) {
        String dateStr = inputDate.format(DateTimeFormatter.ofPattern("yyMMdd"));
        // Lot No 예: ML-260120-MAT01-INIT-OLD
        String lotNo = String.format("ML-%s-%s-%s", dateStr, m.getMaterialCode().substring(13), suffix);

        MaterialLot ml = MaterialLot.builder()
                .material(m)
                .materialLotNo(lotNo)
                .inQty(qty)
                .remainQty(qty)
                .status(MaterialLotStatus.AVAILABLE)
                .inputDate(inputDate) // 입고일 중요 (FIFO 기준)
                .build();
        materialLotRepository.save(ml);

        MaterialTx tx = MaterialTx.builder()
                .txType(TxType.INBOUND)
                .material(m)
                .materialLot(ml)
                .qty(qty)
                .txTime(inputDate)
                .build();
        materialTxRepository.save(tx);
    }

    private void createBom(Product p, Material m, double qty, double scrap) {
        bomRepository.save(BOM.builder()
                .product(p)
                .material(m)
                .requiredQty(BigDecimal.valueOf(qty))
                .scrapRate(BigDecimal.valueOf(scrap))
                .build());
    }

    private ProcessStep createStep(String code, String name, int seq) {
        ProcessStep step = new ProcessStep();
        step.setStepCode(code);
        step.setStepName(name);
        step.setSeq(seq);
        return processStepRepository.save(step);
    }

    private void createMachine(String code, String name, ProcessStep step, boolean active) {
        Machine m = new Machine();
        m.setMachineCode(code);
        m.setMachineName(name);
        m.setProcessCode(step.getStepCode());
        m.setStatus(active ? MachineStatus.RUN : MachineStatus.STOP); // 초기 상태
        m.setIsActive(active);
        machineRepository.save(m);
    }

    // 과거 완료된 이력 (완제품 재고 생성 포함)
    private void createHistoryWorkOrder(Product p, int qty, WorkOrderStatus status, int daysAgo) {
        LocalDateTime pastDate = now().minusDays(daysAgo);
        String woNo = "WO-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";

        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo);
        wo.setProduct(p);
        wo.setPlannedQty(qty);
        wo.setStartedAt(pastDate);
        wo.setDueAt(pastDate.plusHours(8));
        wo.setDueAt(pastDate.plusDays(1));
        wo.setStatus(status);
        wo.setCreatedAt(pastDate);
        workOrderRepository.save(wo);

        // Lot 생성 및 완료 처리
        Lot lot = new Lot();
        lot.setLotNo("LOT-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001");
        lot.setProduct(p);
        lot.setWorkOrder(wo);
        lot.setLotQty(qty);
        lot.setStatus(LotStatus.COMPLETED); // 완료됨
        lot.setCreatedAt(pastDate);
        lotRepository.save(lot);

        // [중요] 완료된 건이므로 완제품 창고(FgInventory)에 재고 등록
        FgInventory fg = FgInventory.builder()
                .product(p)
                .lot(lot)
                .stockQty(qty)
                .locationCode("WH-FG-A01")
                .build();
        fgInventoryRepository.save(fg);
    }

    // 현재 진행 중인 이력
    private void createRunningWorkOrder(Product p, int qty) {
        String today = now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String woNo = "WO-" + today + "-002";

        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo);
        wo.setProduct(p);
        wo.setPlannedQty(qty);
        wo.setStartedAt(now().minusHours(2)); // 2시간 전 시작
        wo.setDueAt(now().plusDays(2));
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo.setCreatedAt(now());
        workOrderRepository.save(wo);

        Lot lot = new Lot();
        lot.setLotNo("LOT-" + today + "-002");
        lot.setProduct(p);
        lot.setWorkOrder(wo);
        lot.setLotQty(qty);
        lot.setStatus(LotStatus.IN_PROGRESS);
        lot.setCreatedAt(now());
        lotRepository.save(lot);
    }

    // 미래 예정된 이력
    private void createPlannedWorkOrder(Product p, int qty, int daysAfter) {
        LocalDateTime futureDate = now().plusDays(daysAfter);
        String woNo = "WO-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";

        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo);
        wo.setProduct(p);
        wo.setPlannedQty(qty);
        wo.setDueAt(futureDate.plusDays(1));
        wo.setStatus(WorkOrderStatus.WAIT);
        wo.setCreatedAt(now());
        workOrderRepository.save(wo);

        // Lot은 아직 발행되지 않았거나, 계획 상태로 생성
        Lot lot = new Lot();
        lot.setLotNo("LOT-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-PLAN");
        lot.setProduct(p);
        lot.setWorkOrder(wo);
        lot.setLotQty(qty);
        lot.setStatus(LotStatus.HOLD); // 생성만 됨
        lot.setCreatedAt(now());
        lotRepository.save(lot);
    }
}