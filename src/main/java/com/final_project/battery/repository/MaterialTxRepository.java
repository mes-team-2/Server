package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.MaterialTx;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.response.MaterialLotHistoryDto;
import com.final_project.battery.dto.response.MaterialTxAllResponseDto;
import com.final_project.battery.dto.response.MaterialTxDetailResponseDto;
import com.final_project.battery.dto.response.MaterialTxResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MaterialTxRepository extends JpaRepository<MaterialTx, Integer> {
    // 해당 Lot 생산에 사용된(CONSUME) 자재 내역 조회
    List<MaterialTx> findByLotAndTxType(Lot lot, TxType txType);

    // 자재 이력 조회 (제품 LOT 추가)
    @Query(
            value = """
        select new com.final_project.battery.dto.response.MaterialTxResponseDto(
            t.materialTxId,
            t.txTime,
            t.txType,
            m.materialName,
            ml.materialLotNo,
            l.lotNo,
            t.qty,
            m.unit
        )
        from MaterialTx t
        join t.material m
        join t.materialLot ml
        left join t.lot l
        where (:type is null or t.txType = :type)
          and (
            :keyword is null or
            lower(m.materialName) like lower(concat('%', :keyword, '%')) or
            lower(ml.materialLotNo) like lower(concat('%', :keyword, '%')) or
            lower(l.lotNo) like lower(concat('%', :keyword, '%'))
          )
          and (:startDate is null or t.txTime >= :startDate)
          and (:endDate is null or t.txTime <= :endDate)
        """,
            countQuery = """
        select count(t)
        from MaterialTx t
        join t.material m
        join t.materialLot ml
        left join t.lot l
        where (:type is null or t.txType = :type)
          and (
            :keyword is null or
            lower(m.materialName) like lower(concat('%', :keyword, '%')) or
            lower(ml.materialLotNo) like lower(concat('%', :keyword, '%')) or
            lower(l.lotNo) like lower(concat('%', :keyword, '%'))
          )
          and (:startDate is null or t.txTime >= :startDate)
          and (:endDate is null or t.txTime <= :endDate)
        """
    )
    Page<MaterialTxResponseDto> search(
            @Param("type") TxType type,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 자재 이력 합계 반환 (기존 유지)
    @Query("""
    select new com.final_project.battery.dto.response.MaterialTxAllResponseDto(
        cast(coalesce(sum(case when t.txType = com.final_project.battery.domain.common.TxType.INBOUND then t.qty else 0 end), 0) as bigdecimal),
        cast(coalesce(sum(case when t.txType = com.final_project.battery.domain.common.TxType.CONSUME then abs(t.qty) else 0 end), 0) as bigdecimal),
        cast(0 as bigdecimal)
    )
    from MaterialTx t
    join t.material m
    join t.materialLot ml
    where (:type is null or t.txType = :type)
      and (
        :keyword is null or
        lower(m.materialName) like lower(concat('%', :keyword, '%')) or
        lower(ml.materialLotNo) like lower(concat('%', :keyword, '%'))
      )
      and (:startDate is null or t.txTime >= :startDate)
      and (:endDate is null or t.txTime <= :endDate)
    """)
    MaterialTxAllResponseDto getSummary(
            @Param("type") TxType type,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 상세 조회 (제품 LOT 추가)
    @Query("""
    SELECT new com.final_project.battery.dto.response.MaterialTxDetailResponseDto(
        t.materialTxId,
        t.txType,
        t.txTime,
        t.qty,
        l.remainQty,
        l.remainQty,
        m.materialCode,
        m.materialName,
        pl.lotNo
    )
    FROM MaterialTx t
    JOIN t.materialLot l
    JOIN t.material m
    LEFT JOIN t.lot pl
    WHERE t.materialTxId = :id
    """)
    MaterialTxDetailResponseDto findDetail(@Param("id") Integer id);

    @Query("""
select new com.final_project.battery.dto.response.MaterialLotHistoryDto(
    max(tx.txTime),
    l.lotNo,
    sum(tx.qty)
)
from MaterialTx tx
join tx.lot l
where tx.materialLot.materialLotId = :lotId
  and tx.txType = com.final_project.battery.domain.common.TxType.CONSUME
group by l.lotNo
order by max(tx.txTime) desc
""")
    List<MaterialLotHistoryDto> findConsumeHistory(Long lotId);
}