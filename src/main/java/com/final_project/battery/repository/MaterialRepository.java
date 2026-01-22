package com.final_project.battery.repository;

import com.final_project.battery.domain.Material;
import com.final_project.battery.dto.response.MaterialInventoryResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByMaterialCode(String materialCode);

    @Query("SELECT new com.final_project.battery.dto.response.MaterialInventoryResponseDto(" +
            "m.materialId, m.materialCode, m.materialName, " +
            "(SELECT COALESCE(SUM(ml.remainQty), 0) FROM MaterialLot ml WHERE ml.material = m AND ml.status = 'AVAILABLE'), " +
            "m.unit, COALESCE(m.safeQty, 1000), " +
            "m.createdAt, " +
            "(SELECT MAX(ml.inputDate) FROM MaterialLot ml WHERE ml.material = m)) " +
            "FROM Material m")
    List<MaterialInventoryResponseDto> findAllWithStock();
}