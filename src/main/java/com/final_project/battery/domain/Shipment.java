package com.final_project.battery.domain;

import com.final_project.battery.domain.common.ShipmentType;
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
    private Long id;

    // 프론트 tx_type
    @Enumerated(EnumType.STRING)
    private ShipmentType txType;

    // in / out (프론트 Status 컴포넌트용)
    @Column(length = 10)
    private String statusKey;

    // 제품 정보 (스냅샷)
    private String productCode;
    private String productName;
    private String productLotNo;

    // 수량 (+입고 / -출고)
    private int qty;

    private String unit;       // EA
    private String location;   // 위치 or 출고처

    private LocalDateTime txTime;
}