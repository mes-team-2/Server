package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProcessStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processStepId;
    @Column(unique = true, nullable = false)
    private String stepCode;
    private String stepName;
    private Integer seq;
}