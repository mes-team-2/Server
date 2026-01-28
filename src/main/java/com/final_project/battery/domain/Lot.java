package com.final_project.battery.domain;

import com.final_project.battery.domain.common.LotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lotId;

    @Column(unique = true, nullable = false)
    private String lotNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    private List<FgInventory> inventories;

    private Integer lotQty;
    @Enumerated(EnumType.STRING)
    private LotStatus status;
    private LocalDateTime createdAt;
}