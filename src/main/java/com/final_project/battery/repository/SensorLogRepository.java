package com.final_project.battery.repository;

import com.final_project.battery.domain.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
}
