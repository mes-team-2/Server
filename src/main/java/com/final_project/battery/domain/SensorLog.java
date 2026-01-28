package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sensor_log")
public class SensorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sensorLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    private Double temperature;
    private Double humidity;
    private Double voltage;

    private LocalDateTime recordedAt;
}