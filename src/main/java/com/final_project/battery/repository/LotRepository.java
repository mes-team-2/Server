package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.WorkOrder;
import com.final_project.battery.domain.common.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LotRepository extends JpaRepository<Lot, Long> {
    Optional<Lot> findByLotNo(String lotNo);
    Optional<Lot> findFirstByWorkOrder(WorkOrder workOrder);
    List<Lot> findByStatus(LotStatus lotStatus);

    // 완성품 재고 상세 lot 리스트
    @Query("""
        select l from Lot l
        join fetch l.product p
        join fetch l.workOrder wo
        left join fetch wo.manager m
        where p.productCode = :productCode
    """)
    List<Lot> findByProductCodeWithDetails(@Param("productCode") String productCode);
}