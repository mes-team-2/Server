package com.final_project.battery.dto.response;

import com.final_project.battery.domain.common.TxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MaterialTxDetailResponseDto {
    private Integer id;
    private TxType txType;
    private LocalDateTime txTime;
    private java.math.BigDecimal qty;
    private java.math.BigDecimal beforeQty;
    private java.math.BigDecimal remainQty;
    private String materialCode;
    private String materialName;
    private String productLotNo;
}
