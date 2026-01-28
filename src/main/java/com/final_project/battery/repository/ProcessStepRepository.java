package com.final_project.battery.repository;

import com.final_project.battery.domain.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
    Optional<ProcessStep> findByStepCode(String stepCode);
}