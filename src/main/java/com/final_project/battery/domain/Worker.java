package com.final_project.battery.domain;

import com.final_project.battery.domain.common.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workerId;
    @Column(unique = true, nullable = false)
    private String workerCode;
    private String password;
    private String workerName;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
}