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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final ProductionLogRepository productionLogRepository;
    private final DefectLogRepository defectLogRepository;

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
        Worker w1 = createWorker("W-260203-0001", "우민규", "1234", Role.ADMIN, true);
        Worker tester = createWorker("test", "테스터", "1234", Role.ADMIN, true);
        Worker w2 = createWorker("W-260203-0002", "이현수", "1234", Role.OPERATOR, true);
        Worker w3 = createWorker("W-260203-0003", "양찬종", "1234", Role.OPERATOR, true);
        Worker w4 = createWorker("W-260203-0004", "김하린", "1234", Role.OPERATOR, true);

        // 추가 인원 (팀 구성을 위해 넉넉히)
        createWorker("W-260203-0005", "이준호", "1234", Role.OPERATOR, false);
        createWorker("W-260203-0006", "박서준", "1234", Role.OPERATOR, true);
        createWorker("W-260203-0007", "최지우", "1234", Role.OPERATOR, false);
        createWorker("W-260203-0008", "정유진", "1234", Role.OPERATOR, true);
        createWorker("W-260203-0009", "강동원", "1234", Role.OPERATOR, false);
        createWorker("W-260203-0010", "한소희", "1234", Role.OPERATOR, true);

        // 2. 공정 및 설비
        ProcessStep s1 = createStep("PROC-010", "전극공정(Electrode)", 10);
        ProcessStep s2 = createStep("PROC-020", "조립공정(Assembly)", 20);
        ProcessStep s3 = createStep("PROC-030", "활성화공정(Formation)", 30);
        ProcessStep s4 = createStep("PROC-040", "팩공정(Pack)", 40);
        ProcessStep s5 = createStep("PROC-050", "검사공정(Inspection)", 50);

        Machine mA01 = createMachine("MAC-A-01", "Electrode M/C #1", s1);
        Machine mA02 = createMachine("MAC-A-02", "Assembly Line #1", s2);
        Machine mA03 = createMachine("MAC-A-03", "Formation Sys #1", s3);
        Machine mA04 = createMachine("MAC-A-04", "Pack Line #1", s4);
        Machine mA05 = createMachine("MAC-A-05", "Inspector #1", s5);

        Machine mB01 = createMachine("MAC-B-01", "Electrode M/C #2", s1);
        Machine mB02 = createMachine("MAC-B-02", "Assembly Line #2", s2);
        Machine mB03 = createMachine("MAC-B-03", "Formation Sys #2", s3);
        Machine mB04 = createMachine("MAC-B-04", "Pack Line #2", s4);
        Machine mB05 = createMachine("MAC-B-05", "Inspector #2", s5);

        // 설비 초기 상태 설정
        setMachineStatus(mA01, "RUN", w2);
        setMachineStatus(mA02, "RUN", w3);
        setMachineStatus(mA03, "RUN", w2);
        setMachineStatus(mA04, "RUN", w3);
        setMachineStatus(mA05, "RUN", w4);

        setMachineStatus(mB01, "STOP", null);
        setMachineError(mB02, "ERROR", w2, "온도 센서 과열");
        setMachineStatus(mB03, "STOP", null);
        setMachineStatus(mB04, "STOP", null);
        setMachineError(mB05, "ERROR", w4, "통신 모듈 응답 없음");

        // 3. 제품/자재/BOM
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

        createMountedLot(mLead, new BigDecimal("5400"), mA01);
        createMountedLot(mPosPlate, new BigDecimal("3800"), mA01);
        createMountedLot(mNegPlate, new BigDecimal("3800"), mA01);
        createMountedLot(mSeparator, new BigDecimal("7600"), mA02);
        createMountedLot(mElectrolyte, new BigDecimal("1800"), mA02);
        createMountedLot(mCase, new BigDecimal("600"), mA02);
        createMountedLot(mCover, new BigDecimal("600"), mA02);
        createMountedLot(mTerminal, new BigDecimal("1200"), mA02);
        createMountedLot(mLabel, new BigDecimal("600"), mA04);

        // BOM (소형/중형/대형 전체)
        createBom(pSmall, mLead, 6.0, 0.0, "전극공정");
        createBom(pSmall, mPosPlate, 5.0, 0.01, "전극공정");
        createBom(pSmall, mNegPlate, 5.0, 0.01, "전극공정");
        createBom(pSmall, mSeparator, 10.0, 0.01, "조립공정");
        createBom(pSmall, mElectrolyte, 2.0, 0.02, "조립공정");
        createBom(pSmall, mCase, 1.0, 0.0, "조립공정");
        createBom(pSmall, mCover, 1.0, 0.0, "조립공정");
        createBom(pSmall, mTerminal, 2.0, 0.0, "조립공정");
        createBom(pSmall, mLabel, 1.0, 0.0, "팩공정");

        createBom(pMedium, mLead, 9.0, 0.0, "전극공정");
        createBom(pMedium, mPosPlate, 6.0, 0.01, "전극공정");
        createBom(pMedium, mNegPlate, 6.0, 0.01, "전극공정");
        createBom(pMedium, mSeparator, 12.0, 0.01, "조립공정");
        createBom(pMedium, mElectrolyte, 3.0, 0.02, "조립공정");
        createBom(pMedium, mCase, 1.0, 0.0, "조립공정");
        createBom(pMedium, mCover, 1.0, 0.0, "조립공정");
        createBom(pMedium, mTerminal, 2.0, 0.0, "조립공정");
        createBom(pMedium, mLabel, 1.0, 0.0, "팩공정");

        createBom(pLarge, mLead, 12.0, 0.0, "전극공정");
        createBom(pLarge, mPosPlate, 8.0, 0.01, "전극공정");
        createBom(pLarge, mNegPlate, 8.0, 0.01, "전극공정");
        createBom(pLarge, mSeparator, 16.0, 0.01, "조립공정");
        createBom(pLarge, mElectrolyte, 4.0, 0.02, "조립공정");
        createBom(pLarge, mCase, 1.0, 0.0, "조립공정");
        createBom(pLarge, mCover, 1.0, 0.0, "조립공정");
        createBom(pLarge, mTerminal, 2.0, 0.0, "조립공정");
        createBom(pLarge, mLabel, 1.0, 0.0, "팩공정");

        // 4. 작업지시 & LOT
//        Lot historyLot = createHistoryWorkOrder(pSmall, 100, WorkOrderStatus.DONE, 7);
//        createDummyQualityLogs(historyLot, mA05, 100, w4);

//        createRunningWorkOrder(pMedium, 50);

        // 5. [핵심] 대시보드용 금일(Today) 데이터 생성
        createDashboardDummyData(Arrays.asList(mA01, mA02, mA03, mA04, mA05), w2);

        System.out.println("🎉 [Real Factory] 데이터 초기화 완료!");
    }

    // ==========================================
    // 대시보드용 더미 데이터 생성 (오늘 날짜)
    // ==========================================
    private void createDashboardDummyData(List<Machine> machines, Worker worker) {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atTime(9, 0); // 오늘 09:00
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startOfToday)) return; // 9시 이전이면 생성 안함

        Random rand = new Random();
        WorkOrder runningOrder = workOrderRepository.findAll().stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .findFirst().orElse(null);

        Lot runningLot = null;
        if(runningOrder != null) {
            runningLot = lotRepository.findFirstByWorkOrder(runningOrder).orElse(null);
        }

        Map<String, ProcessStep> stepMap = processStepRepository.findAll().stream()
                .collect(Collectors.toMap(ProcessStep::getStepCode, Function.identity()));

        // 09:00부터 현재 시간까지 매 시간마다 로그 생성
        LocalDateTime current = startOfToday;
        while (current.isBefore(now)) {
            for (Machine m : machines) {
                // 1. 센서 로그 (매 시간)
                sensorLogRepository.save(SensorLog.builder()
                        .machine(m)
                        .temperature(24.0 + rand.nextDouble() * 5)
                        .humidity(40.0 + rand.nextDouble() * 10)
                        .voltage(220.0 + (rand.nextDouble() - 0.5) * 5)
                        .recordedAt(current)
                        .build());

                // 2. 생산 로그 (가동 중인 설비만)
                if (m.getStatus() == MachineStatus.RUN && runningLot != null) {
                    ProductionLog pl = new ProductionLog();
                    pl.setMachine(m);
                    pl.setWorker(worker);
                    pl.setWorkOrder(runningOrder);
                    pl.setLot(runningLot);

                    // [핵심 수정] 설비의 공정 코드에 맞는 공정 객체(ProcessStep)를 넣어줍니다.
                    // 이게 들어가야 DashboardService에서 'PROC-50'을 찾아 양품으로 집계합니다.
                    if (stepMap.containsKey(m.getProcessCode())) {
                        pl.setProcessStep(stepMap.get(m.getProcessCode()));
                    }

                    pl.setStartedAt(current);
                    pl.setEndedAt(current.plusMinutes(50)); // 50분 가동

                    int produced = 40 + rand.nextInt(20); // 시간당 40~60개 생산
                    int defects = rand.nextInt(3); // 시간당 0~2개 불량

                    pl.setGoodQty(produced - defects);
                    pl.setBadQty(defects);
                    productionLogRepository.save(pl);

                    // 3. 불량 로그 (불량 발생 시)
                    String machineCode = m.getMachineCode();

                    DefectType[] allowedTypes =
                            DEFECT_TYPE_BY_MACHINE.getOrDefault(
                                    machineCode,
                                    DefectType.values() // fallback
                            );

                    for (int i = 0; i < defects; i++) {
                        DefectLog dl = new DefectLog();
                        dl.setMachine(m);
                        dl.setWorker(worker);
                        dl.setLot(runningLot);
                        dl.setProductionLog(pl);
                        dl.setDefectQty(1);
                        dl.setCreatedAt(current.plusMinutes(rand.nextInt(50)));

                        // ✅ 설비별 허용 불량만 랜덤 할당
                        dl.setDefectType(allowedTypes[rand.nextInt(allowedTypes.length)]);

                        defectLogRepository.save(dl);
                    }

                }
            }
            current = current.plusHours(1);
        }

        // 현재 시점의 최신 센서값 하나 더 추가 (대시보드 실시간 현황용)
        for (Machine m : machines) {
            sensorLogRepository.save(SensorLog.builder()
                    .machine(m)
                    .temperature(25.0 + rand.nextDouble())
                    .humidity(45.0 + rand.nextDouble())
                    .voltage(220.0)
                    .recordedAt(now)
                    .build());
        }
    }
    private static final Map<String, DefectType[]> DEFECT_TYPE_BY_MACHINE = Map.of(
            "MAC-A-01", new DefectType[]{DefectType.SCRATCH, DefectType.THICKNESS_ERROR},
            "MAC-A-02", new DefectType[]{DefectType.MISALIGNMENT, DefectType.MISSING_PART},
            "MAC-A-03", new DefectType[]{DefectType.LOW_VOLTAGE, DefectType.HIGH_TEMP},
            "MAC-A-04", new DefectType[]{DefectType.WELDING_ERROR, DefectType.LABEL_ERROR},
            "MAC-A-05", new DefectType[]{DefectType.DIMENSION_ERROR, DefectType.FOREIGN_MATERIAL}
    );


    // ... (기존 createWorker, createMachine, createBom 등 Helper 메서드들은 그대로 유지)
    // 아래 코드는 기존 코드 복사해서 그대로 두시면 됩니다. (지면 관계상 생략하지 않고 핵심만 넣음)

    private void setMachineStatus(Machine m, String status, Worker w) {
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(m); log.setWorker(w); log.setStatus(status);
        log.setStartTime(now().minusHours(4));
        machineStatusLogRepository.save(log);
        m.setStatus(MachineStatus.valueOf(status));
        machineRepository.save(m);
    }

    private void setMachineError(Machine m, String status, Worker w, String reason) {
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(m); log.setWorker(w); log.setStatus(status);
        log.setReasonCode(reason); log.setStartTime(now().minusMinutes(30));
        machineStatusLogRepository.save(log);
        m.setStatus(MachineStatus.valueOf(status));
        machineRepository.save(m);
    }

    private Worker createWorker(String code, String name, String pw, Role role, boolean active) {
        Worker w = new Worker();
        w.setWorkerCode(code); w.setWorkerName(name); w.setPassword(passwordEncoder.encode(pw));
        w.setRole(role); w.setIsActive(active); w.setCreatedAt(now().minusDays(new Random().nextInt(365)));
        return workerRepository.save(w);
    }

    private Machine createMachine(String code, String name, ProcessStep step) {
        Machine m = new Machine();
        m.setMachineCode(code); m.setMachineName(name); m.setProcessCode(step.getStepCode());
        m.setIsActive(true);
        return machineRepository.save(m);
    }

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
        String woNo = "WO-" + today + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(now().minusHours(2)); wo.setDueDate(now().plusDays(2)); wo.setEndedAt(now().minusHours(1)); wo.setStatus(WorkOrderStatus.IN_PROGRESS); wo.setCreatedAt(now());
        workOrderRepository.save(wo);
        lotRepository.save(Lot.builder().lotNo("LOT-" + today + "-002").product(p).workOrder(wo).lotQty(qty).status(LotStatus.IN_PROGRESS).createdAt(now()).build());
    }

    private void createPlannedWorkOrder(Product p, int qty, int daysAfter) {
        LocalDateTime futureDate = now().plusDays(daysAfter);
        String woNo = "WO-" + futureDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(woNo); wo.setProduct(p); wo.setPlannedQty(qty); wo.setStartedAt(now().minusHours(1));wo.setDueDate(futureDate.plusDays(1)); wo.setEndedAt(now()); wo.setStatus(WorkOrderStatus.WAIT); wo.setCreatedAt(now());
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