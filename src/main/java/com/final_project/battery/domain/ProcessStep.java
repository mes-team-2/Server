package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processStepId;
    @Column(unique = true, nullable = false)
    private String stepCode;
    private String stepName;
    private Integer seq;

    @Builder.Default
    private Boolean active = true;
}