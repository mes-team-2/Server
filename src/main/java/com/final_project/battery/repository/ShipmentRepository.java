package com.final_project.battery.repository;

import com.final_project.battery.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 기간 조회
    List<Shipment> findByTxTimeBetweenOrderByTxTimeDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    // 전체 조회
    List<Shipment> findAllByOrderByTxTimeDesc();
}
