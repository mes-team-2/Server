package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.domain.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ProductionLog> findByLotAndProcessStep(Lot lot, ProcessStep processStep);
}