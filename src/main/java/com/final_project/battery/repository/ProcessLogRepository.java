package com.final_project.battery.repository;

import com.final_project.battery.domain.ProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {
}
