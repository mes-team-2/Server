package com.final_project.battery.repository;

import com.final_project.battery.domain.MachineStatusLog;
import com.final_project.battery.domain.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineStatusLogRepository extends JpaRepository<MachineStatusLog, Long> {
    List<MachineStatusLog> findTop10ByWorkerOrderByStartTimeDesc(Worker worker);
}
