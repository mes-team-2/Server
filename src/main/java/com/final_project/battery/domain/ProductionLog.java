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
public class ProductionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productionLogId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lot_id")
    private Lot lot;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "machine_id")
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "process_step_id")
    private ProcessStep processStep;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "worker_id")
    private Worker worker;
    private Integer goodQty = 0;
    private Integer badQty = 0;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}