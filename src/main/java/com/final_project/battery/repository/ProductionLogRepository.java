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
        """
    )
    Page<TestLogResponseDto> searchTestLogs(
            @Param("isOk") Boolean isOk,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}