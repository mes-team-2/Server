package com.final_project.battery.domain;

import com.final_project.battery.domain.common.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workOrderId;

    @Column(unique = true, nullable = false)
    private String workOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer plannedQty;
    private LocalDateTime startedAt;
    private LocalDateTime dueAt;
    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status;
    private LocalDateTime createdAt;
}