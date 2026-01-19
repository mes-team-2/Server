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
            "COALESCE(i.stockQty, 0), m.unit, m.createdAt, i.updatedAt) " +
            "FROM Material m LEFT JOIN Inventory i ON i.material = m")
    List<MaterialInventoryResponseDto> findAllWithStock();
}