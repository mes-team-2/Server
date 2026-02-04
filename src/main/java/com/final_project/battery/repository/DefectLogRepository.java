package com.final_project.battery.repository;

import com.final_project.battery.domain.DefectLog;
import com.final_project.battery.domain.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DefectLogRepository extends JpaRepository<DefectLog, Integer> {
    boolean existsByWorkerAndCreatedAtBetween(Worker worker, LocalDateTime start, LocalDateTime end);
    List<DefectLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}