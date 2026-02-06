package com.final_project.battery.repository;

import com.final_project.battery.domain.FgInventory;
import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.common.InventoryStatus;
import com.final_project.battery.dto.response.FgInventoryManagementResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface FgInventoryRepository extends JpaRepository<FgInventory, Integer> {
    Optional<FgInventory> findByLot(Lot lot);
    List<FgInventory> findByLocationCode(String locationCode);

    // 제품 재고 관리 데이터
    @Query("""
    select new com.final_project.battery.dto.response.FgInventoryManagementResponseDto(
        p.productCode,
        p.productName,
        p.unit,
        sum(f.stockQty),
        max(f.updatedAt)
    )
    from FgInventory f
    join f.product p
    where (
        :keyword is null or
        lower(p.productName) like lower(concat('%', :keyword, '%')) or
        lower(p.productCode) like lower(concat('%', :keyword, '%'))
    )
    and (:startDate is null or f.updatedAt >= :startDate)
    and (:endDate is null or f.updatedAt <= :endDate)
    group by p.productCode, p.productName, p.unit
""")
    List<FgInventoryManagementResponseDto> searchFgInventory(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT function('date_format', f.createdAt, '%Y-%m-%d') as date, " +
            "p.productName as product, " +
            "SUM(f.stockQty) as ok " +
            "FROM FgInventory f " +
            "JOIN f.product p " +
            "WHERE f.createdAt BETWEEN :start AND :end " +
            "GROUP BY function('date_format', f.createdAt, '%Y-%m-%d'), p.productName")
    List<Object[]> findDailyProductionStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}