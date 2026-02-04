package com.final_project.battery.repository;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    Optional<SensorLog> findTopByMachineOrderByRecordedAtDesc(Machine machine);
}
