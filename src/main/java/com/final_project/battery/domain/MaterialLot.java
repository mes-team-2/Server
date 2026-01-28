package com.final_project.battery.domain;

import com.final_project.battery.domain.common.MaterialLotStatus;
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
@Table(name = "material_lot")
public class MaterialLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialLotId;

    // 어떤 자재인가?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    // 자재 Lot 번호 (예: MAT-IN-20260120-001)
    @Column(unique = true, nullable = false)
    private String materialLotNo;

    // 입고 수량 (최초 들어온 양)
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal inQty;

    // 현재 잔여 수량 (FIFO 로직의 핵심)
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal remainQty;

    private LocalDateTime inputDate;
    private LocalDateTime expireDate;

    // 상태 (AVAILABLE, EXHAUSTED)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MaterialLotStatus status = MaterialLotStatus.AVAILABLE;

    // 현재 이 자재가 장착된 설비 (null이면 창고 보관 중)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_machine_id")
    private Machine currentMachine;

    @PrePersist
    public void prePersist() {
        if (this.inputDate == null) this.inputDate = LocalDateTime.now();
    }
}