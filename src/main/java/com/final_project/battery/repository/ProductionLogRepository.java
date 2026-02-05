package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.domain.ProductionLog;
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
            lower(l.lotNo) like lower(concat('%', :keyword, '%')) or
            lower(w.workOrderNo) like lower(concat('%', :keyword, '%')) or
            lower(pr.productName) like lower(concat('%', :keyword, '%')) or
            lower(m.machineName) like lower(concat('%', :keyword, '%')) or
            lower(wk.workerCode) like lower(concat('%', :keyword, '%'))
        )
        and (:startDate is null or p.endedAt >= :startDate)
        and (:endDate is null or p.endedAt <= :endDate)
        and (:defectType is null or d.defectType = :defectType)
        """,
            countQuery = """
        select count(p)
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
            lower(l.lotNo) like lower(concat('%', :keyword, '%')) or
            lower(w.workOrderNo) like lower(concat('%', :keyword, '%')) or
            lower(pr.productName) like lower(concat('%', :keyword, '%')) or
            lower(m.machineName) like lower(concat('%', :keyword, '%')) or
            lower(wk.workerCode) like lower(concat('%', :keyword, '%'))
        )
        and (:startDate is null or p.endedAt >= :startDate)
        and (:endDate is null or p.endedAt <= :endDate)
        and (:defectType is null or d.defectType = :defectType)
        """
    )
    Page<TestLogResponseDto> searchTestLogs(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") String defectType,
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
    lower(l.lotNo) like lower(concat('%', :keyword, '%')) or
    lower(w.workOrderNo) like lower(concat('%', :keyword, '%')) or
    lower(pr.productName) like lower(concat('%', :keyword, '%')) or
    lower(m.machineName) like lower(concat('%', :keyword, '%')) or
    lower(wk.workerCode) like lower(concat('%', :keyword, '%'))
)
and (:startDate is null or p.endedAt >= :startDate)
and (:endDate is null or p.endedAt <= :endDate)
and (:defectType is null or d.defectType = :defectType)
""")
    List<Object[]> getCardSummary(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("defectType") String defectType,
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
    lower(l.lotNo) like lower(concat('%', :keyword, '%')) or
    lower(w.workOrderNo) like lower(concat('%', :keyword, '%')) or
    lower(pr.productName) like lower(concat('%', :keyword, '%')) or
    lower(m.machineName) like lower(concat('%', :keyword, '%')) or
    lower(wk.workerCode) like lower(concat('%', :keyword, '%'))
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
            @Param("defectType") String defectType,
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
    lower(l.lotNo) like lower(concat('%', :keyword, '%')) or
    lower(w.workOrderNo) like lower(concat('%', :keyword, '%')) or
    lower(pr.productName) like lower(concat('%', :keyword, '%')) or
    lower(m.machineName) like lower(concat('%', :keyword, '%')) or
    lower(wk.workerCode) like lower(concat('%', :keyword, '%'))
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
            @Param("defectType") String defectType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );








}