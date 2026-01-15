package com.final_project.battery.repository;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FgInventoryRepository extends JpaRepository<FgInventory, Integer> {
    // 이미 해당 Lot로 잡힌 재고가 있는지 확인 (업데이트용)
    Optional<FgInventory> findByLot(Lot lot);

}