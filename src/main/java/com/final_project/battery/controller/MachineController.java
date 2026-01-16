package com.final_project.battery.controller;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.MachineStatusLog;
import com.final_project.battery.domain.Worker;
import com.final_project.battery.domain.common.MachineStatus;
import com.final_project.battery.repository.MachineRepository;
import com.final_project.battery.repository.MachineStatusLogRepository;
import com.final_project.battery.repository.WorkerRepository;
import com.final_project.battery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/machine")
@RequiredArgsConstructor
public class MachineController {

    private final MachineRepository machineRepository;
    private final MachineStatusLogRepository machineStatusLogRepository;
    private final WorkerRepository workerRepository;

    // 설비 상태 변경 API (RUN, STOP, ERROR)
    @PostMapping("/{machineId}/status")
    @Transactional
    public ResponseEntity<String> updateMachineStatus(
            @PathVariable Long machineId,
            @RequestParam MachineStatus status,
            @RequestParam(required = false) String reason
            ) {
        Long workerId = Long.parseLong(SecurityUtil.getCurrentWorkerCode());

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("작업자 정보가 유효하지 않습니다."));

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 설비입니다."));

        // 상태 변경
        machine.setStatus(status);

        // 이력 로그 저장 (MachineStatusLog)
        MachineStatusLog log = new MachineStatusLog();
        log.setMachine(machine);
        log.setWorker(worker);
        log.setStatus(status.name());
        log.setReasonCode(reason);
        log.setStartTime(LocalDateTime.now());

        machineStatusLogRepository.save(log);

        return ResponseEntity.ok("설비 상태 변경 완료: " + status);
    }
}
