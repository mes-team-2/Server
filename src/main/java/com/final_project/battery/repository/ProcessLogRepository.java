package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {
    // Lot 번호로 공정 이력 조회
    List<ProcessLog> findByLotOrderByStartTimeAsc(Lot lot);
}
