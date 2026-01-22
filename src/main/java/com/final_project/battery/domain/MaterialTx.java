package com.final_project.battery.domain;

import com.final_project.battery.domain.common.TxType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialTx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer materialTxId;

    @Enumerated(EnumType.STRING)
    private TxType txType; // INBOUND, CONSUME

    // [추가] 어떤 자재 Lot가 입/출고 되었는가?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_lot_id")
    private MaterialLot materialLot;

    // 기존 Material 참조는 유지 (조회 편의성)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    private java.math.BigDecimal qty;
    private LocalDateTime txTime;

    @PrePersist
    public void prePersist() {
        if(this.txTime == null) this.txTime = LocalDateTime.now();
    }
}