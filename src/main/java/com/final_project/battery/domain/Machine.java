package com.final_project.battery.domain;

import com.final_project.battery.domain.common.MachineStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long machineId;
    @Column(unique = true, nullable = false)
    private String machineCode;
    private String machineName;
    private String processCode;
    @Enumerated(EnumType.STRING)
    private MachineStatus status;
    private Boolean isActive = false;
}