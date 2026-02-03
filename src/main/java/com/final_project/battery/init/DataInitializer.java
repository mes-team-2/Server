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
import java.util.Arrays;
import java.util.List;
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

        // ==========================================
        // 1. 작업자 생성 (출근/퇴근 상태 혼합)
        // ==========================================
        // [핵심 멤버 - 출근(ON)]
        Worker w1 = createWorker("W-260203-0001", "우민규", "1234", Role.ADMIN, true);
        Worker w2 = createWorker("W-260203-0002", "김현수", "1234", Role.OPERATOR, true);
        Worker w3 = createWorker("W-260203-0003", "양찬종", "1234", Role.OPERATOR, true);
        Worker w4 = createWorker("W-260203-0004", "김하린", "1234", Role.OPERATOR, true);

        // [추가 멤버 - 일부는 퇴근(OFF) 처리]
        createWorker("W-260203-0005", "이준호", "1234", Role.OPERATOR, false); // 퇴근
        createWorker("W-260203-0006", "박서준", "1234", Role.OPERATOR, true);  // 출근
        createWorker("W-260203-0007", "최지우", "1234", Role.OPERATOR, false); // 퇴근
        createWorker("W-260203-0008", "정유진", "1234", Role.OPERATOR, true);  // 출근
        createWorker("W-260203-0009", "강동원", "1234", Role.OPERATOR, false); // 퇴근
        createWorker("W-260203-0010", "한소희", "1234", Role.OPERATOR, true);  // 출근

        // ==========================================
        // 2. 공정 및 설비 생성 (상태 다양화)
        // ==========================================
        ProcessStep s1 = createStep("PROC-10", "전극공정(Electrode)", 10);
        ProcessStep s2 = createStep("PROC-20", "조립공정(Assembly)", 20);
        ProcessStep s3 = createStep("PROC-30", "활성화공정(Formation)", 30);
        ProcessStep s4 = createStep("PROC-40", "팩공정(Pack)", 40);
        ProcessStep s5 = createStep("PROC-50", "검사공정(Inspection)", 50);

        // [A라인: 가동 중]
        Machine mA01 = createMachine("MAC-A-01", "Electrode M/C #1", s1);
        Machine mA02 = createMachine("MAC-A-02", "Assembly Line #1", s2);
        Machine mA03 = createMachine("MAC-A-03", "Formation Sys #1", s3);
        Machine mA04 = createMachine("MAC-A-04", "Pack Line #1", s4);
        Machine mA05 = createMachine("MAC-A-05", "Inspector #1", s5);

        // [B라인: 에러, 정지, 대기 섞기]
        Machine mB01 = createMachine("MAC-B-01", "Electrode M/C #2", s1);
        Machine mB02 = createMachine("MAC-B-02", "Assembly Line #2", s2);
        Machine mB03 = createMachine("MAC-B-03", "Formation Sys #2", s3);
        Machine mB04 = createMachine("MAC-B-04", "Pack Line #2", s4);
        Machine mB05 = createMachine("MAC-B-05", "Inspector #2", s5);

        // ==========================================
        // 3. 설비 상태 & 로그 설정 (요청 사항 반영)
        // ==========================================

        // A라인
        setMachineStatus(mA01, "WAIT", w2);
        setMachineStatus(mA02, "WAIT", w3);
        setMachineStatus(mA03, "WAIT", w2);
        setMachineStatus(mA04, "WAIT", w3);
        setMachineStatus(mA05, "WAIT", w4);

        // B라인
        setMachineStatus(mB01, "STOP", null);        // 정지
        setMachineError(mB02, "ERROR", w2, "온도 센서 과열 (Overheat)"); // 에러
        setMachineStatus(mB03, "STOP", null);        // 대기
        setMachineStatus(mB04, "STOP", null);        // 정지
        setMachineError(mB05, "ERROR", w4, "비전 카메라 통신 불량"); // 에러

        // ==========================================
        // 4. 제품 & 자재 & BOM (기존 동일)
        // ==========================================
        Product pSmall = createProduct("BAT-12V-45AH", "12V 소형 배터리", 45, 12, "EA");
        Product pMedium = createProduct("BAT-12V-65AH", "12V 중형 배터리", 65, 12, "EA");
        Product pLarge = createProduct("BAT-12V-90AH", "12V 대형 배터리", 90, 12, "EA");

        Material mLead = createMaterial("납(Pb)", "KG", 5000);
        Material mPosPlate = createMaterial("양극판", "EA", 10000);
        Material mNegPlate = createMaterial("음극판", "EA", 10000);
        Material mSeparator = createMaterial("분리판", "EA", 50000);
        Material mElectrolyte = createMaterial("전해액", "L", 5000);
        Material mCase = createMaterial("케이스", "EA", 1000);
        Material mCover = createMaterial("커버", "EA", 1000);
        Material mTerminal = createMaterial("단자", "EA", 5000);
        Material mLabel = createMaterial("라벨", "EA", 5000);

        // 자재 장착 (A라인 위주)
        createMountedLot(mLead, new BigDecimal("5400"), mA01);
        createMountedLot(mPosPlate, new BigDecimal("3800"), mA01);
        createMountedLot(mNegPlate, new BigDecimal("3800"), mA01);
        createMountedLot(mSeparator, new BigDecimal("7600"), mA02);
        createMountedLot(mElectrolyte, new BigDecimal("1800"), mA02);
        createMountedLot(mCase, new BigDecimal("600"), mA02);
        createMountedLot(mCover, new BigDecimal("600"), mA02);
        createMountedLot(mTerminal, new BigDecimal("1200"), mA02);
        createMountedLot(mLabel, new BigDecimal("600"), mA04);

        // BOM
        createBom(pSmall, mLead, 6.0, 0.0, "전극공정");
        createBom(pSmall, mPosPlate, 5.0, 0.01, "전극공정");
        createBom(pSmall, mNegPlate, 5.0, 0.01, "전극공정");
        createBom(pSmall, mSeparator, 10.0, 0.01, "조립공정");
        createBom(pSmall, mElectrolyte, 2.0, 0.02, "조립공정");
        createBom(pSmall, mCase, 1.0, 0.0, "조립공정");
        createBom(pSmall, mCover, 1.0, 0.0, "조립공정");
        createBom(pSmall, mTerminal, 2.0, 0.0, "조립공정");
        createBom(pSmall, mLabel, 1.0, 0.0, "팩공정");

        // ==========================================
        // 5. 생산 이력 및 센서 로그
        // ==========================================
        Lot historyLot = createHistoryWorkOrder(pSmall, 100, WorkOrderStatus.DONE, 7);
        createDummyQualityLogs(historyLot, mA05, 100, w4);

        createRunningWorkOrder(pMedium, 100);
        createPlannedWorkOrder(pLarge, 100, 1);

        // 과거 이력 생성 (작업자 상세 페이지용)
        createPastMachineLogs(mA01, w2, 3);
        createPastMachineLogs(mA02, w3, 3);

        createDummySensorLogs(mA01);
        createDummySensorLogs(mA02);

        // 에러난 설비에도 센서 데이터는 찍힘 (이상치)
        createDummySensorLogs(mB02);

        System.out.println("🎉 [Real Factory] 데이터 초기화 완료!");
        System.out.println("   - 작업자: 10명 (출근 6명, 퇴근 4명)");
        System.out.println("   - 설비: 10대 (RUN 5대, STOP 2대, ERROR 2대, WAIT 1대)");
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private void setMachineStatus(Machine m, String status, Worker w) {
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(m);
        log.setWorker(w);
        log.setStatus(status);
        log.setStartTime(now().minusHours(4));
        // EndTime이 null이면 현재 진행 중인 상태
        machineStatusLogRepository.save(log);

        m.setStatus(MachineStatus.valueOf(status));
        machineRepository.save(m);
    }

    private void setMachineError(Machine m, String status, Worker w, String reason) {
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(m);
        log.setWorker(w);
        log.setStatus(status);
        log.setReasonCode(reason); // 에러 사유
        log.setStartTime(now().minusMinutes(30)); // 30분 전 고장
        machineStatusLogRepository.save(log);

        m.setStatus(MachineStatus.valueOf(status));
        machineRepository.save(m);
    }

    private Worker createWorker(String code, String name, String pw, Role role, boolean active) {
        Worker w = new Worker();
        w.setWorkerCode(code);
        w.setWorkerName(name);
        w.setPassword(passwordEncoder.encode(pw));
        w.setRole(role);
        w.setIsActive(active); // 출/퇴근 상태 설정
        w.setCreatedAt(now().minusDays(new Random().nextInt(365)));
        return workerRepository.save(w);
    }

    private Machine createMachine(String code, String name, ProcessStep step) {
        Machine m = new Machine();
        m.setMachineCode(code);
        m.setMachineName(name);
        m.setProcessCode(step.getStepCode());
        m.setIsActive(true); // 설비 자체는 사용 중 (고장나도 설비는 active)
        return machineRepository.save(m);
    }

    // ... (나머지 createBom, createProduct, createMaterial 등은 기존 로직 유지) ...

    private void createBom(Product p, Material m, double qty, double scrap, String process) {
        bomRepository.save(BOM.builder().product(p).material(m).requiredQty(BigDecimal.valueOf(qty)).scrapRate(BigDecimal.valueOf(scrap)).note(process).build());
    }

    private Product createProduct(String code, String name, int cap, int volt, String unit) {
        return productRepository.save(Product.builder().productCode(code).productName(name).capacityAh(cap).voltage(volt).unit(unit).build());
    }

    private Material createMaterial(String name, String unit, int safeQty) {
        String code = String.format("MAT-%s-%04d", now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), matSeq++);
        return materialRepository.save(Material.builder().materialCode(code).materialName(name).unit(unit).safeQty(safeQty).createdAt(now()).build());
    }

    private void createMountedLot(Material m, BigDecimal qty, Machine machine) {
        String lotNo = String.format("ML-%s-%s-INIT", now().minusDays(1).format(DateTimeFormatter.ofPattern("yyMMdd")), m.getMaterialCode().substring(13));
        MaterialLot ml = MaterialLot.builder().material(m).materialLotNo(lotNo).inQty(qty).remainQty(qty).status(MaterialLotStatus.AVAILABLE).inputDate(now().minusDays(1)).currentMachine(machine).build();
        materialLotRepository.save(ml);
        materialTxRepository.save(MaterialTx.builder().txType(TxType.INBOUND).material(m).materialLot(ml).qty(qty).txTime(now().minusDays(1)).build());
    }

    private ProcessStep createStep(String code, String name, int seq) {
        ProcessStep step = new ProcessStep();
        step.setStepCode(code); step.setStepName(name); step.setSeq(seq);
        return processStepRepository.save(step);
    }

    private Lot createHistoryWorkOrder(Product p, int qty, WorkOrderStatus status, int daysAgo) {
        LocalDateTime pastDate = now().minusDays(daysAgo);
        String woNo = "WO-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(pastDate); wo.setDueDate(pastDate.plusDays(1)); wo.setEndedAt(pastDate.plusHours(5)); wo.setStatus(status); wo.setCreatedAt(pastDate);
        workOrderRepository.save(wo);
        Lot lot = Lot.builder().lotNo("LOT-" + pastDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001").product(p).workOrder(wo).lotQty(qty).status(LotStatus.COMPLETED).createdAt(pastDate).build();
        lotRepository.save(lot);
        fgInventoryRepository.save(FgInventory.builder().product(p).lot(lot).stockQty(qty).locationCode("WH-FG-A01").build());
        return lot;
    }

    private void createRunningWorkOrder(Product p, int qty) {
        String today = now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String woNo = "WO-" + today + "-002";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(now().minusHours(2)); wo.setDueDate(now().plusDays(2)); wo.setEndedAt(now().minusHours(1)); wo.setStatus(WorkOrderStatus.DONE); wo.setCreatedAt(now());
        workOrderRepository.save(wo);
        lotRepository.save(Lot.builder().lotNo("LOT-" + today + "-002").product(p).workOrder(wo).lotQty(qty).status(LotStatus.IN_PROGRESS).createdAt(now()).build());
    }

    private void createPlannedWorkOrder(Product p, int qty, int daysAfter) {
        LocalDateTime futureDate = now().plusDays(daysAfter);
        String woNo = "WO-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(now().minusHours(1));wo.setDueDate(futureDate.plusDays(1)); wo.setEndedAt(now()); wo.setStatus(WorkOrderStatus.DONE); wo.setCreatedAt(now());
        workOrderRepository.save(wo);
        lotRepository.save(Lot.builder().lotNo("LOT-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-PLAN").product(p).workOrder(wo).lotQty(qty).status(LotStatus.HOLD).createdAt(now()).build());
    }

    private void createPastMachineLogs(Machine m, Worker w, int count) {
        for(int i=1; i<=count; i++) {
            MachineStatusLog log = new MachineStatusLog();
            log.setMachine(m); log.setWorker(w); log.setStatus("RUN");
            log.setStartTime(now().minusDays(i).minusHours(8));
            log.setEndTime(now().minusDays(i).minusHours(1));
            machineStatusLogRepository.save(log);
        }
    }

    private void createDummyQualityLogs(Lot lot, Machine inspector, int qty, Worker worker) {
        Random random = new Random();
        for(int i=0; i<qty; i++) {
            boolean isFail = random.nextInt(100) < 5;
            QualityTest qt = new QualityTest();
            qt.setLot(lot); qt.setMachine(inspector); qt.setWorker(worker);
            qt.setResult(isFail ? QualityTestResult.FAIL : QualityTestResult.PASS);
            qt.setTestedAt(lot.getCreatedAt().plusMinutes(i * 2L));
            qualityTestRepository.save(qt);
        }
    }

    private void createDummySensorLogs(Machine m) {
        LocalDateTime baseTime = now().minusMinutes(10);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sensorLogRepository.save(SensorLog.builder().machine(m).temperature(25.0 + (random.nextDouble() * 2 - 1)).humidity(45.0 + (random.nextDouble() * 4 - 2)).voltage(220.0 + (random.nextDouble() * 2 - 1)).recordedAt(baseTime.plusMinutes(i)).build());
        }
    }
}