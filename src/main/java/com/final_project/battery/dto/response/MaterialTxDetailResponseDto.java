package com.final_project.battery.dto.response;

import com.final_project.battery.domain.common.TxType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class MaterialTxDetailResponseDto {

    private Long txId;                 // 트랜잭션 ID
    private LocalDateTime occurredAt;  // 발생 일시
    private TxType type;               // IN / OUT / USE

    private String materialCode;       // 자재 코드
    private String materialName;       // 자재명
    private String lotNo;              // LOT 번호

    private BigDecimal qty;            // 이동 수량
    private BigDecimal remainQty;      // 잔량
    private String unit;               // 단위

    private String fromLocation;       // 출발지
    private String toLocation;         // 도착지
    private String operator;           // 작업자
    private String note;               // 비고

    // ⭐ 생성자 단 1개 (JPQL / nativeQuery 대응)
    public MaterialTxDetailResponseDto(
            Long txId,
            LocalDateTime occurredAt,
            TxType type,
            String materialCode,
            String materialName,
            String lotNo,
            BigDecimal qty,
            BigDecimal remainQty,
            String unit,
            String fromLocation,
            String toLocation,
            String operator,
            String note
    ) {
        this.txId = txId;
        this.occurredAt = occurredAt;
        this.type = type;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.lotNo = lotNo;
        this.qty = qty;
        this.remainQty = remainQty;
        this.unit = unit;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.operator = operator;
        this.note = note;
    }
}
