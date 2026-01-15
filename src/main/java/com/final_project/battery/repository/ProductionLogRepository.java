package com.final_project.battery.repository;

import com.final_project.battery.domain.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
}