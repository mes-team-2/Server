package com.final_project.battery.repository;

import com.final_project.battery.domain.Inventory;
import com.final_project.battery.domain.MaterialLot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByMaterialLot(MaterialLot materialLot);
}