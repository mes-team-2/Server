package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inventoryId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "material_id")
    private Material material;
    private java.math.BigDecimal stockQty = java.math.BigDecimal.ZERO;
    private LocalDateTime updatedAt = LocalDateTime.now();
}