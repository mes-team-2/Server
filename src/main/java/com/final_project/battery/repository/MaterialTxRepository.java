package com.final_project.battery.repository;

import com.final_project.battery.domain.MaterialTx;
import com.final_project.battery.dto.response.MaterialTxResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialTxRepository extends JpaRepository<MaterialTx, Integer> {
    @Query("""
    select new com.final_project.battery.dto.response.MaterialTxResponseDto(
        t.txTime,
        t.txType,
        m.materialName,
        t.qty,
        m.unit
    )
    from MaterialTx t
    join t.material m
    """)
    List<MaterialTxResponseDto> materialTxList();
}