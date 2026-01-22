package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inventoryId;

    // Material -> MaterialLot 연결 (자재 Lot별 재고 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_lot_id", nullable = false)
    private MaterialLot materialLot;

    // 사실상 materialLot.remainQty와 중복될 수 있으나,
    // Inventory 테이블은 '현재 가용 재고'를 조회하는 용도로 사용
    private BigDecimal stockQty;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}