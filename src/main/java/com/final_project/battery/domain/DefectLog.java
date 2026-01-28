package com.final_project.battery.domain;

import com.final_project.battery.domain.common.DefectType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DefectLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer defectLogId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lot_id")
    private Lot lot;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "production_log_id")
    private ProductionLog productionLog;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "machine_id")
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "worker_id")
    private Worker worker;
    @Enumerated(EnumType.STRING)
    private DefectType defectType;
    private Integer defectQty = 1;
    private LocalDateTime createdAt = LocalDateTime.now();

    private Double temperature;
    private Double voltage;
    private Double humidity;
}