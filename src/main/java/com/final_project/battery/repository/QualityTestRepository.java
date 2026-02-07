package com.final_project.battery.repository;

import com.final_project.battery.domain.QualityTest;
import com.final_project.battery.dto.response.TraceSummaryResponseDto;
import com.final_project.battery.dto.response.TraceabilityResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface QualityTestRepository extends JpaRepository<QualityTest, Long> {

    @Query(
            value = """
select new com.final_project.battery.dto.response.TraceabilityResponseDto(
     l.lotNo,
     p.productName,

     max(plFinal.endedAt),

     count(distinct plFinal.productionLogId),

     count(distinct case
         when plFinal.goodQty = 1
         then plFinal.productionLogId
     end),

     count(distinct case
         when plFinal.badQty = 1
         then plFinal.productionLogId
     end),

     case
         when count(distinct plFinal.productionLogId) = 0 then 0.0
         else (
             count(distinct case
                 when plFinal.goodQty = 1
                 then plFinal.productionLogId
             end) * 100.0
             / count(distinct plFinal.productionLogId)
         )
     end
)

from Lot l
join l.product p

join ProductionLog plFinal
  on plFinal.lot = l
 and plFinal.processStep.id = 5

where (
    :keyword is null
    or l.lotNo like %:keyword%
    or exists (
        select 1
        from MaterialLot ml
        join ml.material mat2
        where ml.materialLotNo like %:keyword%
          and mat2 in (
              select b3.material
              from BOM b3
              where b3.product = p
          )
    )
)

  and (:start is null or plFinal.endedAt >= :start)
  and (:end is null or plFinal.endedAt <= :end)

  and (
    :machine is null or exists (
        select 1
        from ProductionLog pl2
        join pl2.machine m2
        where pl2.lot = l
          and m2.machineCode = :machine
    )
  )

  and (
    :process is null or exists (
        select 1
        from ProductionLog pl3
        join pl3.processStep ps2
        where pl3.lot = l
          and ps2.stepCode = :process
    )
  )

  and (
    :material is null or exists (
        select 1
        from BOM b2
        join b2.material m2
        where b2.product = p
          and m2.materialName = :material
    )
  )

group by l.lotNo, p.productName
order by max(plFinal.endedAt) desc
""",
            countQuery = """
select count(distinct l)
from Lot l
join l.product p
join ProductionLog plFinal
  on plFinal.lot = l
 and plFinal.processStep.id = 5

where (
    :keyword is null
    or l.lotNo like %:keyword%
    or exists (
        select 1
        from MaterialLot ml
        join ml.material mat2
        where ml.materialLotNo like %:keyword%
          and mat2 in (
              select b3.material
              from BOM b3
              where b3.product = p
          )
    )
)

  and (:start is null or plFinal.endedAt >= :start)
  and (:end is null or plFinal.endedAt <= :end)

  and (
    :machine is null or exists (
        select 1
        from ProductionLog pl2
        join pl2.machine m2
        where pl2.lot = l
          and m2.machineCode = :machine
    )
  )

  and (
    :process is null or exists (
        select 1
        from ProductionLog pl3
        join pl3.processStep ps2
        where pl3.lot = l
          and ps2.stepCode = :process
    )
  )

  and (
    :material is null or exists (
        select 1
        from BOM b2
        join b2.material m2
        where b2.product = p
          and m2.materialName = :material
    )
  )
"""
    )
    Page<TraceabilityResponseDto> searchTrace(
            @Param("keyword") String keyword,
            @Param("machine") String machine,
            @Param("process") String process,
            @Param("material") String material,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 추적성 집계
    @Query("""
select new com.final_project.battery.dto.response.TraceSummaryResponseDto(

    count(distinct l.id),

    coalesce(sum(plFinal.goodQty + plFinal.badQty), 0),

    coalesce(sum(plFinal.goodQty), 0),

    coalesce(sum(plFinal.badQty), 0)
)

from Lot l
join l.product p

join ProductionLog plFinal
  on plFinal.lot = l
 and plFinal.processStep.id = 5

where (
    :keyword is null
    or l.lotNo like %:keyword%
    or exists (
        select 1
        from MaterialLot ml
        join ml.material mat2
        where ml.materialLotNo like %:keyword%
          and mat2 in (
              select b3.material
              from BOM b3
              where b3.product = p
          )
    )
)

and (:start is null or plFinal.endedAt >= :start)
and (:end is null or plFinal.endedAt <= :end)

and (
  :machine is null or exists (
      select 1
      from ProductionLog pl2
      join pl2.machine m2
      where pl2.lot = l
        and m2.machineCode = :machine
  )
)

and (
  :process is null or exists (
      select 1
      from ProductionLog pl3
      join pl3.processStep ps2
      where pl3.lot = l
        and ps2.stepCode = :process
  )
)

and (
  :material is null or exists (
      select 1
      from BOM b2
      join b2.material m2
      where b2.product = p
        and m2.materialName = :material
  )
)
""")
    TraceSummaryResponseDto searchTraceSummary(
            @Param("keyword") String keyword,
            @Param("machine") String machine,
            @Param("process") String process,
            @Param("material") String material,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );




}
