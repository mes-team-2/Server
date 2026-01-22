package com.final_project.battery.domain;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    // 출하 번호
    @Column(unique = true, nullable = false)
    private String shipmentNo;

    // 출하 대상 완제품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fg_inventory_id", nullable = false)
    private FgInventory fgInventory;

    // 고객사
    private String customerName;

    // 출하 수량
    private int quantity;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SHIPPED";

    // 출하 일자
    private LocalDateTime shippedAt;

    // 담당자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;
}