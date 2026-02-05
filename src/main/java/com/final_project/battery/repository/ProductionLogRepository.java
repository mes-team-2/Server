package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.domain.ProductionLog;
import com.final_project.battery.domain.common.DefectType;
import com.final_project.battery.dto.response.ProductReportResponse;
import com.final_project.battery.dto.response.TestLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ProductionLog> findByLotAndProcessStep(Lot lot, ProcessStep processStep);

    // 검사이력 조회
    @Query(
            value = """
        select new com.final_project.battery.dto.response.TestLogResponseDto(
            pr.productName,
            p.endedAt,
            case when p.badQty = 0 then true else false end,
            d.defectType,
            l.lotNo,
            w.workOrderNo,
            ps.stepName,
            m.machineName,
            p.temperature,
            p.humidity,
            p.voltage,
            wk.workerCode
        )
        from ProductionLog p
        join p.lot l
        join l.product pr
        join p.workOrder w
        join p.processStep ps
        join p.machine m
        join p.worker wk
        left join DefectLog d on d.productionLog.productionLogId = p.productionLogId
        where
            (:isOk is null or 
                (:isOk = true and p.badQty = 0) or
                (:isOk = false and p.badQty > 0)
            )
        and (
            :keyword is null or
            l.lotNo like concat('%', :keyword, '%') or
            w.workOrderNo like concat('%', :keyword, '%') or
            pr.productName like concat('%', :keyword, '%') or
            m.machineName like concat('%', :keyword, '%') or
            wk.workerCode like concat('%', :keyword, '%')
        )
        and (:startDate is null or p.endedAt >= :startDate)
        and (:endDate is null or p.endedAt <= :endDate)
        and (:defectType is null or d.defectType = :defectType)
        """,
            countQuery = """
        select count(distinct p)
        from ProductionLog p
        join p.lot l
        join l.product pr
        join p.workOrder w
        join p.processStep ps
        join p.machine m
        join p.worker wk
        left join DefectLog d on d.productionLog.productionLogId = p.productionLogId
        where
            (:isOk is null or 
                (:isOk = true and p.badQty = 0) or
                (:isOk = false and p.badQty > 0)
            )
        and (
             :keyword is null or
            l.lotNo like concat('%', :keyword, '%') or
            w.workOrderNo like concat('%', :keyword, '%') or
            pr.productName like concat('%', :keyword, '%') or
            m.machineName like concat('%', :keyword, '%') or
            wk.workerCode like concat('%', :keyword, '%')
        )
        and (:startDate is null or p.endedAt >= :startDate)
        and (:endDate is null or p.endedAt <= :endDate)
        and (:defectType is null or d.defectType = :defectType)
        """
    )
    Page<TestLogResponseDto> searchTestLogs(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") DefectType defectType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 검사이력 카드용 집계
    @Query("""
select
    count(p),
    sum(case when p.badQty = 0 then 1 else 0 end),
    sum(case when p.badQty > 0 then 1 else 0 end)
from ProductionLog p
join p.lot l
join l.product pr
join p.workOrder w
join p.machine m
join p.worker wk
left join DefectLog d on d.productionLog.productionLogId = p.productionLogId
where
    (:isOk is null or 
        (:isOk = true and p.badQty = 0) or
        (:isOk = false and p.badQty > 0)
    )
and (
    :keyword is null or
            l.lotNo like concat('%', :keyword, '%') or
            w.workOrderNo like concat('%', :keyword, '%') or
            pr.productName like concat('%', :keyword, '%') or
            m.machineName like concat('%', :keyword, '%') or
            wk.workerCode like concat('%', :keyword, '%')
)
and (:startDate is null or p.endedAt >= :startDate)
and (:endDate is null or p.endedAt <= :endDate)
and (:defectType is null or d.defectType = :defectType)
""")
    List<Object[]> getCardSummary(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") DefectType defectType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



    // 검사이력 일자별 그래프용 ok/ng 카운트
    @Query("""
select
    function('date_format', p.endedAt, '%Y-%m-%d'),
    sum(case when p.badQty = 0 then 1 else 0 end),
    sum(case when p.badQty > 0 then 1 else 0 end)
from ProductionLog p
join p.lot l
join l.product pr
join p.workOrder w
join p.machine m
join p.worker wk
left join DefectLog d on d.productionLog.productionLogId = p.productionLogId
where
    (:isOk is null or 
        (:isOk = true and p.badQty = 0) or
        (:isOk = false and p.badQty > 0)
    )
and (
    :keyword is null or
            l.lotNo like concat('%', :keyword, '%') or
            w.workOrderNo like concat('%', :keyword, '%') or
            pr.productName like concat('%', :keyword, '%') or
            m.machineName like concat('%', :keyword, '%') or
            wk.workerCode like concat('%', :keyword, '%')
)
and (:startDate is null or p.endedAt >= :startDate)
and (:endDate is null or p.endedAt <= :endDate)
and (:defectType is null or d.defectType = :defectType)

group by function('date_format', p.endedAt, '%Y-%m-%d')
order by function('date_format', p.endedAt, '%Y-%m-%d')
""")
    List<Object[]> getDailySummary(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") DefectType defectType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );




    // 검사이력 불량 유형 집계
    @Query("""
select
    d.defectType,
    count(d)
from DefectLog d
join d.productionLog p
join p.lot l
join l.product pr
join p.workOrder w
join p.machine m
join p.worker wk
where
    (:isOk is null or 
        (:isOk = true and p.badQty = 0) or
        (:isOk = false and p.badQty > 0)
    )
and (
     :keyword is null or
           l.lotNo like concat('%', :keyword, '%') or
            w.workOrderNo like concat('%', :keyword, '%') or
            pr.productName like concat('%', :keyword, '%') or
            m.machineName like concat('%', :keyword, '%') or
            wk.workerCode like concat('%', :keyword, '%')
)
and (:startDate is null or p.endedAt >= :startDate)
and (:endDate is null or p.endedAt <= :endDate)
and (:defectType is null or d.defectType = :defectType)

group by d.defectType
order by count(d) desc
""")
    List<Object[]> getDefectSummary(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") DefectType defectType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



    List<ProductionLog> findByLot(Lot lot);

    @Query(
            value = """
            SELECT 
                DATE(pl.started_at) AS date,
                p.product_name,
                SUM(wo.planned_qty) AS plan_qty,
                SUM(pl.good_qty + pl.bad_qty) AS total_attempt_qty,
                SUM(pl.good_qty) AS good_qty,
                SUM(pl.bad_qty) AS bad_qty,
                CASE 
                    WHEN SUM(pl.good_qty + pl.bad_qty) = 0 THEN 0
                    ELSE SUM(pl.good_qty) * 100.0 / SUM(pl.good_qty + pl.bad_qty)
                END AS yield_rate,
                CASE 
                    WHEN SUM(pl.good_qty + pl.bad_qty) = 0 THEN 0
                    ELSE SUM(pl.bad_qty) * 100.0 / SUM(pl.good_qty + pl.bad_qty)
                END AS defect_rate
            FROM production_log pl
            JOIN work_order wo ON pl.work_order_id = wo.work_order_id
            JOIN product p ON wo.product_id = p.product_id
            WHERE (:start IS NULL OR pl.started_at >= :start)
              AND (:end IS NULL OR pl.started_at <= :end)
              AND (:productName IS NULL OR p.product_name LIKE CONCAT('%', :productName, '%'))
            GROUP BY DATE(pl.started_at), p.product_name
        """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT 1
                FROM production_log pl
                JOIN work_order wo ON pl.work_order_id = wo.work_order_id
                JOIN product p ON wo.product_id = p.product_id
                WHERE (:start IS NULL OR pl.started_at >= :start)
                  AND (:end IS NULL OR pl.started_at <= :end)
                  AND (:productName IS NULL OR p.product_name LIKE CONCAT('%', :productName, '%'))
                GROUP BY DATE(pl.started_at), p.product_name
            ) t
        """,
            nativeQuery = true
    )
    Page<Object[]> getProductReportRaw(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("productName") String productName,
            Pageable pageable
    );
















}