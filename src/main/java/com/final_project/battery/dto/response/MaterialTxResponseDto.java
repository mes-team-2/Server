package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Lot;
import com.final_project.battery.domain.common.TxType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class MaterialTxResponseDto {
    private Integer id;
    private LocalDateTime txTime;
    private TxType txType;
    private String materialName;
    private String materialNo;
    private String productLotNo;
    private java.math.BigDecimal qty;
    private String unit;

    // ✅ 생성자 단 1개
    public MaterialTxResponseDto(
            Integer id,
            LocalDateTime txTime,
            TxType txType,
            String materialName,
            String materialNo,
            String productLotNo,
            java.math.BigDecimal qty,
            String unit
    ) {
        this.id = id;
        this.txTime = txTime;
        this.txType = txType;
        this.materialName = materialName;
        this.materialNo = materialNo;
        this.productLotNo = productLotNo;
        this.qty = qty;
        this.unit = unit;
    }
}
