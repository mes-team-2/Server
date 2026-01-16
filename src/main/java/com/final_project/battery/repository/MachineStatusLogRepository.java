package com.final_project.battery.repository;

import com.final_project.battery.domain.MachineStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineStatusLogRepository extends JpaRepository<MachineStatusLog, Long> {
}
