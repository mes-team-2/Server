package com.final_project.battery.domain;

import com.final_project.battery.domain.common.QualityTestResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QualityTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualityTestId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lot_id")
    private Lot lot;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "machine_id")
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "worker_id")
    private Worker worker;
    @Enumerated(EnumType.STRING)
    private QualityTestResult result; // PASS, FAIL
    private LocalDateTime testedAt = LocalDateTime.now();
}