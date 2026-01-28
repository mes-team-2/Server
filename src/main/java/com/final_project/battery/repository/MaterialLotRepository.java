package com.final_project.battery.repository;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.Material;
import com.final_project.battery.domain.MaterialLot;
import com.final_project.battery.domain.common.MaterialLotStatus;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaterialLotRepository extends JpaRepository<MaterialLot, Long> {

    // [핵심] 선입선출(FIFO)을 위해 '가장 먼저 들어온 순서(inputDate ASC)'로 조회
    // 단, 재고가 남아있는(AVAILABLE) 것만 가져옴
    @Query("SELECT ml FROM MaterialLot ml WHERE ml.material = :material AND ml.status = 'AVAILABLE' ORDER BY ml.inputDate ASC")
    List<MaterialLot> findAvailableLotsByMaterial(@Param("material") Material material);

    Optional<MaterialLot> findFirstByMaterial_MaterialIdAndCurrentMachineIsNullAndStatusOrderByInputDateAsc(
            Long materialId, MaterialLotStatus status);

    List<MaterialLot> findByMaterialOrderByInputDateDesc(Material material);

    List<MaterialLot> findByCurrentMachineAndStatus(Machine currentMachine, MaterialLotStatus status);
}