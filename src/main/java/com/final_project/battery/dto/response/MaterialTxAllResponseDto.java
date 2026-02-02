package com.final_project.battery.dto.response;


import lombok.Getter;

@Getter
public class MaterialTxAllResponseDto {


    private java.math.BigDecimal inQty;
    private java.math.BigDecimal outUseQty;
    private java.math.BigDecimal rate;


    // ✅ 생성자 단 1개
    public MaterialTxAllResponseDto(
            java.math.BigDecimal inQty,
            java.math.BigDecimal outUseQty,
            java.math.BigDecimal rate

    ) {

        this.inQty = inQty;
        this.outUseQty = outUseQty;
        this.rate = rate;

    }
}
