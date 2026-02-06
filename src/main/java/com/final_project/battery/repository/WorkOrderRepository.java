package com.final_project.battery.repository;

import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByWorkOrderNo(String workOrderNo);
    Optional<WorkOrder> findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT function('date_format', w.startedAt, '%Y-%m-%d') as date, " +
            "p.productName as product, " +
            "SUM(w.plannedQty) as plan " +
            "FROM WorkOrder w " +
            "JOIN w.product p " +
            "WHERE w.startedAt BETWEEN :start AND :end " +
            "GROUP BY function('date_format', w.startedAt, '%Y-%m-%d'), p.productName")
    List<Object[]> findDailyPlanStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
