package com.final_project.battery.repository;

import com.final_project.battery.domain.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByWorkerCode(String workerCode);
}
