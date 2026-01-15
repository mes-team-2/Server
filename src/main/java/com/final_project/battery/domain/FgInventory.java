package com.final_project.battery.domain;

import com.final_project.battery.domain.common.InventoryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "fg_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FgInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fgInventoryId;

    // 완제품 정보 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 생산 Lot 정보 (N : 1)
    // 하나의 Lot가 여러 창고 위치로 쪼개져서 들어갈 수 있으므로 N:1 관계가 적절
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @Column(nullable = false)
    private Integer stockQty; // 현재 재고 수량

    @Column(nullable = false)
    private String locationCode; // 창고 위치 (예: WH-A-01)

    @Enumerated(EnumType.STRING) // DB에는 문자열로 저장 ("AVAILABLE")
    @Column(nullable = false)
    @Builder.Default // 빌더 패턴 사용 시 기본값 적용
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = InventoryStatus.AVAILABLE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}