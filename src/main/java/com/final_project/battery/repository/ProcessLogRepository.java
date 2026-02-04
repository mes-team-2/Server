package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {
    // Lot 번호로 공정 이력 조회
    List<ProcessLog> findByLotOrderByStartTimeAsc(Lot lot);

    @Query("SELECT pl FROM ProcessLog pl " +
            "JOIN FETCH pl.lot l " +
            "JOIN FETCH pl.processStep ps " +
            "JOIN FETCH pl.machine m " +
            "LEFT JOIN FETCH pl.worker w " +
            "WHERE (:start IS NULL OR pl.startTime >= :start) " +
            "AND (:end IS NULL OR pl.endTime <= :end) " +
            "AND (:keyword IS NULL OR " +
            "     l.lotNo LIKE %:keyword% OR " +
            "     ps.stepName LIKE %:keyword% OR " +
            "     m.machineName LIKE %:keyword%) " +
            "ORDER BY pl.startTime DESC")
    List<ProcessLog> search(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end,
                            @Param("keyword") String keyword);
}
