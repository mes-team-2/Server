package com.final_project.battery.repository;

import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByWorkOrderNo(String workOrderNo);
    Optional<WorkOrder> findFirstByStatusOrderByStartedAtDesc(WorkOrderStatus status);
}
