package com.final_project.battery.repository;

import com.final_project.battery.domain.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
}
