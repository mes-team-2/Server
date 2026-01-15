package com.final_project.battery.repository;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FgInventoryRepository extends JpaRepository<FgInventory, Integer> {
    Optional<FgInventory> findByLot(Lot lot);
    List<FgInventory> findByLocationCode(String locationCode);
}