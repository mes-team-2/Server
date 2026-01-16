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
public class MachineStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusLogId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "machine_id")
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "worker_id")
    private Worker worker;
    private String status;
    private String reasonCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}