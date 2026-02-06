package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.domain.ProductionLog;
import com.final_project.battery.domain.common.DefectType;
import com.final_project.battery.dto.response.TestLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ProductionLog> findByLotAndProcessStep(Lot lot, ProcessStep processStep);

    // [New] 일자별, 제품별 불량 수량 집계 (5공정 '검사' 단계만)
    // endedAt 기준
    @Query("SELECT function('date_format', p.endedAt, '%Y-%m-%d') as date, " +
            "prod.productName as product, " +
            "SUM(p.badQty) as ng " +
            "FROM ProductionLog p " +
            "JOIN p.lot l " +
            "JOIN l.product prod " +
            "JOIN p.processStep ps " +
            "WHERE p.endedAt BETWEEN :start AND :end " +
            "AND (ps.stepName LIKE '%검사%' OR ps.stepName LIKE '%Inspection%') " +
            "GROUP BY function('date_format', p.endedAt, '%Y-%m-%d'), prod.productName")
    List<Object[]> findDailyDefectStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // [New] 오늘 공정별 생산/불량 집계 (차트용)
    @Query("SELECT ps.stepName, SUM(p.goodQty), SUM(p.badQty) " +
            "FROM ProductionLog p " +
            "JOIN p.processStep ps " +
            "WHERE p.endedAt BETWEEN :start AND :end " +
            "GROUP BY ps.stepName, ps.seq " +
            "ORDER BY ps.seq ASC")
    List<Object[]> findTodayProcessStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

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
















}