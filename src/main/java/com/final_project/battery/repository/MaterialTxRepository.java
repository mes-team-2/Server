package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.MaterialTx;
import com.final_project.battery.domain.common.TxType;
import com.final_project.battery.dto.response.MaterialTxResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialTxRepository extends JpaRepository<MaterialTx, Integer> {
    // 해당 Lot 생산에 사용된(CONSUME) 자재 내역 조회
    List<MaterialTx> findByLotAndTxType(Lot lot, TxType txType);

    // 자재 이력 조회
    @Query(
            value = """
    select new com.final_project.battery.dto.response.MaterialTxResponseDto(
        t.txTime,
        t.txType,
        m.materialName,
        t.qty,
        m.unit
    )
    from MaterialTx t
    join t.material m
    """,
    countQuery = """
        select count(t)
        from MaterialTx t
        join t.material m
        """)
    Page<MaterialTxResponseDto> listAll(Pageable pageable);
}