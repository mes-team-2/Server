package com.final_project.battery.init;

import com.final_project.battery.domain.*;
import com.final_project.battery.domain.common.LotStatus;
import com.final_project.battery.domain.common.MachineStatus;
import com.final_project.battery.domain.common.Role;
import com.final_project.battery.domain.common.WorkOrderStatus;
import com.final_project.battery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WorkerRepository workerRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final BomRepository bomRepository;
    private final ProcessStepRepository processStepRepository;
    private final MachineRepository machineRepository;
    private final InventoryRepository inventoryRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LotRepository lotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 중복 실행 방지
        if (workerRepository.count() > 0 && productRepository.count() > 0) {
            System.out.println(">> 초기 데이터가 이미 존재합니다.");
            return;
        }

        System.out.println(">> 초기 데이터 생성을 시작합니다...");

        // 1. 작업자 (Worker) 생성
        initWorkers();

        // 2. 제품 (Product) 3종 생성
        initProducts();

        // 3. 자재 (Material) 7종 생성
        initMaterials();

        // 4. BOM (자재 소요량) 연결
        initBOMs();

        // 5. 공정 및 설비 생성 (5단계)
        initProcessAndMachines();

        // 6. [추가] 초기 자재 재고 입고 (테스트용 10,000개씩)
        initMaterialStock();

        // 7. [추가] 테스트용 Lot 생성 (시뮬레이터 연동용)
        initTestLot();

        System.out.println(">> 초기 데이터 및 테스트 Lot 생성 완료!");
    }

    private void initWorkers() {
        String encodedPassword = passwordEncoder.encode("1234");

        Worker operator = new Worker();
        operator.setWorkerCode("W001");
        operator.setWorkerName("홍길동");
        operator.setPassword(encodedPassword);
        operator.setRole(Role.OPERATOR);
        operator.setIsActive(true);

        Worker admin = new Worker();
        admin.setWorkerCode("A001");
        admin.setWorkerName("김관리");
        admin.setPassword(encodedPassword);
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);

        workerRepository.saveAll(Arrays.asList(operator, admin));
    }

    private void initProducts() {
        productRepository.save(Product.builder().productCode("PROD-001").productName("12V 배터리 소형 (45Ah)").capacityAh(45).voltage(12).unit("EA").build());
        productRepository.save(Product.builder().productCode("PROD-002").productName("12V 배터리 중형 (65Ah)").capacityAh(65).voltage(12).unit("EA").build());
        productRepository.save(Product.builder().productCode("PROD-003").productName("12V 배터리 대형 (90Ah)").capacityAh(90).voltage(12).unit("EA").build());
    }

    private void initMaterials() {
        materialRepository.save(Material.builder().materialCode("MAT-PLATE-POS").materialName("양극판 (Positive Plate)").unit("EA").createdAt(now()).build());
        materialRepository.save(Material.builder().materialCode("MAT-PLATE-NEG").materialName("음극판 (Negative Plate)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-SEPARATOR").materialName("PE 격리판").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-ACID").materialName("황산 (Electrolyte)").unit("L").build());
        materialRepository.save(Material.builder().materialCode("MAT-TERM").materialName("납 단자 (Terminal)").unit("EA").build());

        materialRepository.save(Material.builder().materialCode("MAT-CASE-S").materialName("PP 케이스 (소)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-COVER-S").materialName("PP 커버 (소)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-CASE-M").materialName("PP 케이스 (중)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-COVER-M").materialName("PP 커버 (중)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-CASE-L").materialName("PP 케이스 (대)").unit("EA").build());
        materialRepository.save(Material.builder().materialCode("MAT-COVER-L").materialName("PP 커버 (대)").unit("EA").build());
    }

    private void initBOMs() {
        Product p1 = getProduct("PROD-001");
        Product p2 = getProduct("PROD-002");
        Product p3 = getProduct("PROD-003");

        // [소형 45Ah]
        createBom(p1, "MAT-PLATE-POS", 5.0, 0.0);
        createBom(p1, "MAT-PLATE-NEG", 6.0, 0.0);
        createBom(p1, "MAT-SEPARATOR", 10.0, 0.05);
        createBom(p1, "MAT-ACID", 2.5, 0.1);
        createBom(p1, "MAT-TERM", 2.0, 0.0);
        createBom(p1, "MAT-CASE-S", 1.0, 0.0);
        createBom(p1, "MAT-COVER-S", 1.0, 0.0);

        // [중형 65Ah]
        createBom(p2, "MAT-PLATE-POS", 7.0, 0.0);
        createBom(p2, "MAT-PLATE-NEG", 8.0, 0.0);
        createBom(p2, "MAT-SEPARATOR", 14.0, 0.05);
        createBom(p2, "MAT-ACID", 3.5, 0.1);
        createBom(p2, "MAT-TERM", 2.0, 0.0);
        createBom(p2, "MAT-CASE-M", 1.0, 0.0);
        createBom(p2, "MAT-COVER-M", 1.0, 0.0);

        // [대형 90Ah]
        createBom(p3, "MAT-PLATE-POS", 9.0, 0.0);
        createBom(p3, "MAT-PLATE-NEG", 10.0, 0.0);
        createBom(p3, "MAT-SEPARATOR", 18.0, 0.05);
        createBom(p3, "MAT-ACID", 4.5, 0.1);
        createBom(p3, "MAT-TERM", 2.0, 0.0);
        createBom(p3, "MAT-CASE-L", 1.0, 0.0);
        createBom(p3, "MAT-COVER-L", 1.0, 0.0);
    }

    private void initProcessAndMachines() {
        ProcessStep s1 = createStep("PROC-01", "전극 공정", 1);
        ProcessStep s2 = createStep("PROC-02", "조립 공정", 2);
        ProcessStep s3 = createStep("PROC-03", "활성화 공정", 3);
        ProcessStep s4 = createStep("PROC-04", "팩 공정", 4);
        ProcessStep s5 = createStep("PROC-05", "검사 공정", 5);

        createMachine("M-ELEC-01", "전극 코팅기 #1", s1);
        createMachine("M-ASSY-01", "조립 라인 #1", s2);
        createMachine("M-FORM-01", "활성화 장비 #1", s3);
        createMachine("M-PACK-01", "팩 조립기 #1", s4);
        createMachine("M-INSP-01", "최종 검사기 #1", s5);
    }

    private void initMaterialStock() {
        materialRepository.findAll().forEach(material -> {
            Inventory inventory = new Inventory();
            inventory.setMaterial(material);
            inventory.setStockQty(new BigDecimal("10000")); // 10,000개씩 입고
            inventoryRepository.save(inventory);
        });
    }

    private void initTestLot() {
        // [추가됨] 테스트용 Lot 데이터 생성 로직
        Product product = getProduct("PROD-001"); // 소형 배터리

        // 1. 작업지시 생성
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo("WO-20240115-001");
        wo.setProduct(product);
        wo.setPlannedQty(100);
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrderRepository.save(wo);

        // 2. LOT 생성
        Lot lot = new Lot();
        lot.setLotNo("LOT-20240115-001");
        lot.setProduct(product);
        lot.setWorkOrder(wo);
        lot.setLotQty(100);
        lot.setStatus(LotStatus.IN_PROGRESS);
        lotRepository.save(lot);
    }

    // --- Helper Methods ---

    private Product getProduct(String code) {
        return productRepository.findByProductCode(code)
                .orElseThrow(() -> new RuntimeException("Product not found: " + code));
    }

    private void createBom(Product product, String matCode, double qty, double scrap) {
        Material material = materialRepository.findByMaterialCode(matCode)
                .orElseThrow(() -> new RuntimeException("Material not found: " + matCode));

        bomRepository.save(BOM.builder()
                .product(product)
                .material(material)
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

    private void createMachine(String code, String name, ProcessStep step) {
        Machine machine = new Machine();
        machine.setMachineCode(code);
        machine.setMachineName(name);
        machine.setProcessCode(step.getStepCode());
        machine.setStatus(MachineStatus.STOP);
        machine.setIsActive(true);
        machineRepository.save(machine);
    }
}