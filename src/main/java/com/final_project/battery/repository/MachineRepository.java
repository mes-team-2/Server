package com.final_project.battery.repository;

import com.final_project.battery.domain.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByMachineCode(String machineCode);
}