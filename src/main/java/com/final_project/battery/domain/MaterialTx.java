package com.final_project.battery.domain;

import com.final_project.battery.domain.common.TxType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MaterialTx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer materialTxId;
    @Enumerated(EnumType.STRING)
    private TxType txType; // INBOUND, CONSUME
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "material_id")
    private Material material;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lot_id")
    private Lot lot;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;
    private java.math.BigDecimal qty;
    private LocalDateTime txTime = LocalDateTime.now();
}