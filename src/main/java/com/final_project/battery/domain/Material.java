package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialId;

    @Column(unique = true, nullable = false)
    private String materialCode;

    @Column(nullable = false)
    private String materialName;

    @Column(nullable = false)
    private String unit;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    @ColumnDefault("1000")
    private Integer safeQty;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}