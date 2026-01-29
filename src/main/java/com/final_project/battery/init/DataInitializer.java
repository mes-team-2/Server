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
import java.util.Random;

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
    private final FgInventoryRepository fgInventoryRepository;
    private final SensorLogRepository sensorLogRepository;

    // 추가 리포지토리
    private final MachineStatusLogRepository machineStatusLogRepository;
    private final QualityTestRepository qualityTestRepository;

    private int matSeq = 1;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (workerRepository.count() > 0) {
            System.out.println("⚠️ 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("🏭 [Real Factory Mode] 공장 초기 데이터를 생성합니다...");

        // 1. 작업자 생성
        createWorker("OP-001", "김반장", "1234", Role.OPERATOR);
        createWorker("OP-002", "이작업", "1234", Role.OPERATOR);
        createWorker("QC-001", "박품질", "1234", Role.OPERATOR);
        createWorker("MGR-01", "최관리", "1234", Role.ADMIN);
        createWorker("SYSTEM", "시스템", "1234", Role.ADMIN);

        // 2. 공정 및 설비 생성
        ProcessStep s1 = createStep("PROC-10", "전극공정(Electrode)", 10);
        ProcessStep s2 = createStep("PROC-20", "조립공정(Assembly)", 20);
        ProcessStep s3 = createStep("PROC-30", "활성화공정(Formation)", 30);
        ProcessStep s4 = createStep("PROC-40", "팩공정(Pack)", 40);
        ProcessStep s5 = createStep("PROC-50", "검사공정(Inspection)", 50);

        Machine mA01 = createMachine("MAC-A-01", "Electrode M/C #1", s1, true);
        Machine mA02 = createMachine("MAC-A-02", "Assembly Line #1", s2, true);
        Machine mA03 = createMachine("MAC-A-03", "Formation Sys #1", s3, true);
        Machine mA04 = createMachine("MAC-A-04", "Pack Line #1", s4, true);
        Machine mA05 = createMachine("MAC-A-05", "Inspector #1", s5, true);

        // 3. 제품 라인업
        Product pSmall = createProduct("BAT-12V-45AH", "12V 소형 배터리 (Compact)", 45, 12, "EA");
        Product pMedium = createProduct("BAT-12V-65AH", "12V 중형 배터리 (Standard)", 65, 12, "EA");
        Product pLarge = createProduct("BAT-12V-90AH", "12V 대형 배터리 (Heavy)", 90, 12, "EA");

        // 4. 자재 생성
        Material mLead = createMaterial("납(Pb)", "KG", 5000);
        Material mPosPlate = createMaterial("양극판", "EA", 10000);
        Material mNegPlate = createMaterial("음극판", "EA", 10000);
        Material mSeparator = createMaterial("분리판", "EA", 50000);
        Material mElectrolyte = createMaterial("전해액", "L", 5000);
        Material mCase = createMaterial("케이스", "EA", 1000);
        Material mCover = createMaterial("커버", "EA", 1000);
        Material mTerminal = createMaterial("단자", "EA", 5000);
        Material mLabel = createMaterial("라벨", "EA", 5000);

        // 5. 자재 LOT 생성 및 설비 장착
        createMountedLot(mLead, new BigDecimal("5400"), mA01);
        createMountedLot(mPosPlate, new BigDecimal("3800"), mA01);
        createMountedLot(mNegPlate, new BigDecimal("3800"), mA01);

        createMountedLot(mSeparator, new BigDecimal("7600"), mA02);
        createMountedLot(mElectrolyte, new BigDecimal("1800"), mA02);
        createMountedLot(mCase, new BigDecimal("600"), mA02);
        createMountedLot(mCover, new BigDecimal("600"), mA02);
        createMountedLot(mTerminal, new BigDecimal("1200"), mA02);

        createMountedLot(mLabel, new BigDecimal("600"), mA04);

        // 6. BOM 생성
        createBom(pSmall, mLead, 6.0, 0.0);
        createBom(pSmall, mPosPlate, 5.0, 0.01);
        createBom(pSmall, mNegPlate, 5.0, 0.01);
        createBom(pSmall, mSeparator, 10.0, 0.01);
        createBom(pSmall, mElectrolyte, 2.0, 0.02);
        createBom(pSmall, mCase, 1.0, 0.0);
        createBom(pSmall, mCover, 1.0, 0.0);
        createBom(pSmall, mTerminal, 2.0, 0.0);
        createBom(pSmall, mLabel, 1.0, 0.0);

        createBom(pMedium, mLead, 9.0, 0.0);
        createBom(pMedium, mPosPlate, 6.0, 0.01);
        createBom(pMedium, mNegPlate, 6.0, 0.01);
        createBom(pMedium, mSeparator, 12.0, 0.01);
        createBom(pMedium, mElectrolyte, 3.0, 0.02);
        createBom(pMedium, mCase, 1.0, 0.0);
        createBom(pMedium, mCover, 1.0, 0.0);
        createBom(pMedium, mTerminal, 2.0, 0.0);
        createBom(pMedium, mLabel, 1.0, 0.0);

        createBom(pLarge, mLead, 12.0, 0.0);
        createBom(pLarge, mPosPlate, 8.0, 0.01);
        createBom(pLarge, mNegPlate, 8.0, 0.01);
        createBom(pLarge, mSeparator, 16.0, 0.01);
        createBom(pLarge, mElectrolyte, 4.0, 0.02);
        createBom(pLarge, mCase, 1.0, 0.0);
        createBom(pLarge, mCover, 1.0, 0.0);
        createBom(pLarge, mTerminal, 2.0, 0.0);
        createBom(pLarge, mLabel, 1.0, 0.0);

        // ==========================================
        // 7. 생산 이력 및 로그 시뮬레이션
        // ==========================================

        // Case 1: [완료] 지난주 생산 완료 (소형 100개) -> Batch Lot 1개로 생성
        Lot historyLot = createHistoryWorkOrder(pSmall, 100, WorkOrderStatus.DONE, 7);
        // 완료된 Lot에 대한 품질 검사 이력 생성
        createDummyQualityLogs(historyLot, mA05, 100);

        // Case 2: [진행중] 현재 생산 중 (중형 100개)
        // 시뮬레이터가 접속하면 이 작업을 가져가서 시작합니다.
        createRunningWorkOrder(pMedium, 100);

        // Case 3: [대기] 내일 예정 (대형 100개)
        createPlannedWorkOrder(pLarge, 100, 1);

        // 초기 설비 상태 생성
        createInitialMachineStatus(mA01);
        createInitialMachineStatus(mA02);
        createInitialMachineStatus(mA03);
        createInitialMachineStatus(mA04);
        createInitialMachineStatus(mA05);

        // 센서 로그
        createDummySensorLogs(mA01);
        createDummySensorLogs(mA02);
        createDummySensorLogs(mA03);
        createDummySensorLogs(mA04);
        createDummySensorLogs(mA05);

        System.out.println("🎉 [Real Factory] 데이터 초기화 완료! (소/중/대 각 100개분, Batch Lot 적용)");
    }

    // ==================================================================================
    // Helper Methods
    // ==================================================================================

    private void createInitialMachineStatus(Machine m) {
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(m);
        log.setWorker(workerRepository.findByWorkerCode("SYSTEM").orElse(null));
        log.setStatus("WAIT");
        log.setStartTime(now().minusHours(24));
        log.setEndTime(null);
        machineStatusLogRepository.save(log);

        m.setStatus(MachineStatus.WAIT);
        machineRepository.save(m);
    }

    private void createDummyQualityLogs(Lot lot, Machine inspector, int qty) {
        Worker qcWorker = workerRepository.findByWorkerCode("QC-001").orElse(null);
        Random random = new Random();

        for(int i=0; i<qty; i++) {
            boolean isFail = random.nextInt(100) < 5;
            QualityTest qt = new QualityTest();
            qt.setLot(lot);
            qt.setMachine(inspector);
            qt.setWorker(qcWorker);
            qt.setResult(isFail ? QualityTestResult.FAIL : QualityTestResult.PASS);
            qt.setTestedAt(lot.getCreatedAt().plusMinutes(i * 2L));
            qualityTestRepository.save(qt);
        }
    }

    private void createDummySensorLogs(Machine m) {
        LocalDateTime baseTime = now().minusMinutes(10);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            SensorLog log = SensorLog.builder()
                    .machine(m)
                    .temperature(25.0 + (random.nextDouble() * 2 - 1))
                    .humidity(45.0 + (random.nextDouble() * 4 - 2))
                    .voltage(220.0 + (random.nextDouble() * 2 - 1))
                    .recordedAt(baseTime.plusMinutes(i))
                    .build();
            sensorLogRepository.save(log);
        }
    }

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
        Product p = Product.builder().productCode(code).productName(name).capacityAh(cap).voltage(volt).unit(unit).build();
        return productRepository.save(p);
    }

    private Material createMaterial(String name, String unit, int safeQty) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String code = String.format("MAT-%s-%04d", dateStr, matSeq++);
        Material m = Material.builder().materialCode(code).materialName(name).unit(unit).safeQty(safeQty).createdAt(now()).build();
        return materialRepository.save(m);
    }

    private void createMountedLot(Material m, BigDecimal qty, Machine machine) {
        String dateStr = now().minusDays(1).format(DateTimeFormatter.ofPattern("yyMMdd"));
        String lotNo = String.format("ML-%s-%s-INIT", dateStr, m.getMaterialCode().substring(13));
        MaterialLot ml = MaterialLot.builder()
                .material(m).materialLotNo(lotNo).inQty(qty).remainQty(qty)
                .status(MaterialLotStatus.AVAILABLE).inputDate(now().minusDays(1))
                .currentMachine(machine).build();
        materialLotRepository.save(ml);
        MaterialTx tx = MaterialTx.builder()
                .txType(TxType.INBOUND).material(m).materialLot(ml).qty(qty).txTime(now().minusDays(1)).build();
        materialTxRepository.save(tx);
    }

    private void createBom(Product p, Material m, double qty, double scrap) {
        bomRepository.save(BOM.builder().product(p).material(m).requiredQty(BigDecimal.valueOf(qty)).scrapRate(BigDecimal.valueOf(scrap)).build());
    }

    private ProcessStep createStep(String code, String name, int seq) {
        ProcessStep step = new ProcessStep();
        step.setStepCode(code);
        step.setStepName(name);
        step.setSeq(seq);
        return processStepRepository.save(step);
    }

    private Machine createMachine(String code, String name, ProcessStep step, boolean active) {
        Machine m = new Machine();
        m.setMachineCode(code); m.setMachineName(name); m.setProcessCode(step.getStepCode());
        m.setStatus(active ? MachineStatus.RUN : MachineStatus.STOP); m.setIsActive(active);
        return machineRepository.save(m);
    }

    // [수정] Batch Lot 개념 적용 (WO당 1개의 Lot)
    private Lot createHistoryWorkOrder(Product p, int qty, WorkOrderStatus status, int daysAgo) {
        LocalDateTime pastDate = now().minusDays(daysAgo);
        String woNo = "WO-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(pastDate);
        wo.setDueDate(pastDate.plusDays(1)); wo.setEndedAt(pastDate.plusHours(5)); wo.setStatus(status); wo.setCreatedAt(pastDate);
        workOrderRepository.save(wo);

        // Batch Lot 생성
        Lot lot = Lot.builder()
                .lotNo("LOT-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001")
                .product(p)
                .workOrder(wo)
                .lotQty(qty)
                .status(LotStatus.COMPLETED)
                .createdAt(pastDate)
                .build();
        lotRepository.save(lot);

        FgInventory fg = FgInventory.builder().product(p).lot(lot).stockQty(qty).locationCode("WH-FG-A01").build();
        fgInventoryRepository.save(fg);

        return lot;
    }

    private void createRunningWorkOrder(Product p, int qty) {
        String today = now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String woNo = "WO-" + today + "-002";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(now().minusHours(2));
        wo.setDueDate(now().plusDays(2)); wo.setEndedAt(null); wo.setStatus(WorkOrderStatus.IN_PROGRESS); wo.setCreatedAt(now());
        workOrderRepository.save(wo);

        // Batch Lot 미리 생성 (설비들이 이 Lot에 기록을 누적함)
        Lot lot = Lot.builder()
                .lotNo("LOT-" + today + "-002")
                .product(p)
                .workOrder(wo)
                .lotQty(qty)
                .status(LotStatus.IN_PROGRESS)
                .createdAt(now())
                .build();
        lotRepository.save(lot);
    }

    private void createPlannedWorkOrder(Product p, int qty, int daysAfter) {
        LocalDateTime futureDate = now().plusDays(daysAfter);
        String woNo = "WO-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setDueDate(futureDate.plusDays(1)); wo.setEndedAt(null);
        wo.setStatus(WorkOrderStatus.WAIT); wo.setCreatedAt(now());
        workOrderRepository.save(wo);

        Lot lot = Lot.builder()
                .lotNo("LOT-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-PLAN")
                .product(p)
                .workOrder(wo)
                .lotQty(qty)
                .status(LotStatus.HOLD)
                .createdAt(now())
                .build();
        lotRepository.save(lot);
    }
}