package com.final_project.battery.repository;

import com.final_project.battery.domain.DefectLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefectLogRepository extends JpaRepository<DefectLog, Integer> {
}