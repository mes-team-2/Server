package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.MaterialTx;
import com.final_project.battery.domain.common.TxType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialTxRepository extends JpaRepository<MaterialTx, Integer> {
    // 해당 Lot 생산에 사용된(CONSUME) 자재 내역 조회
    List<MaterialTx> findByLotAndTxType(Lot lot, TxType txType);
}