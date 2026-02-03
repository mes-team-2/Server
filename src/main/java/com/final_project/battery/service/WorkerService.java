package com.final_project.battery.service;

import com.final_project.battery.domain.MachineStatusLog;
import com.final_project.battery.domain.Worker;
import com.final_project.battery.domain.common.Role;
import com.final_project.battery.dto.request.WorkerCreateDto;
import com.final_project.battery.dto.request.WorkerUpdateDto;
import com.final_project.battery.dto.response.WorkerDetailDto;
import com.final_project.battery.dto.response.WorkerHistoryDto;
import com.final_project.battery.dto.response.WorkerResponseDto;
import com.final_project.battery.repository.DefectLogRepository;
import com.final_project.battery.repository.MachineStatusLogRepository;
import com.final_project.battery.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final MachineStatusLogRepository machineStatusLogRepository; // 추가 필요
    private final DefectLogRepository defectLogRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 등록된 Bean 필요

    // [New] 작업자 상세 조회 (이력 포함)
    @Transactional(readOnly = true)
    public WorkerDetailDto getWorkerDetail(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("작업자 없음"));

        WorkerDetailDto detail = new WorkerDetailDto();
        detail.setWorkerInfo(new WorkerResponseDto(worker));

        // 1. 최근 작업 로그 조회 (최신순 10개)
        List<MachineStatusLog> logs = machineStatusLogRepository.findTop10ByWorkerOrderByStartTimeDesc(worker);

        // 2. 현재 상태 판단 (가장 최신 로그가 종료되지 않았으면 '근무중')
        if (!logs.isEmpty() && logs.get(0).getEndTime() == null) {
            detail.setCurrentStatus("근무중");
        } else {
            // 퇴사자가 아니면 '대기', 퇴사자면 '퇴사'
            detail.setCurrentStatus(worker.getIsActive() ? "대기" : "퇴사");
        }

        // 3. 이력 DTO 변환
        List<WorkerHistoryDto> historyList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        for (MachineStatusLog log : logs) {
            WorkerHistoryDto h = new WorkerHistoryDto();
            h.setId(log.getStatusLogId());
            // 설비명을 통해 공정 유추 (Machine Entity에 processCode가 있음)
            h.setProcess(log.getMachine().getMachineName());
            h.setStartTime(log.getStartTime().format(formatter));
            h.setEndTime(log.getEndTime() != null ? log.getEndTime().format(formatter) : "-");

            // 해당 시간대 불량 발생 여부 확인 (간이 로직)
            // 실제로는 ProductionLog와 조인해야 정확하지만, 여기서는 시간대로 추정
            boolean hasDefect = false;
            if (log.getEndTime() != null) {
                hasDefect = defectLogRepository.existsByWorkerAndCreatedAtBetween(
                        worker, log.getStartTime(), log.getEndTime());
            }
            h.setDefect(hasDefect ? "불량 발생" : "없음");

            historyList.add(h);
        }
        detail.setHistory(historyList);

        return detail;
    }

    @Transactional(readOnly = true)
    public List<WorkerResponseDto> getAllWorkers() {
        return workerRepository.findAll().stream()
                .map(WorkerResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createWorker(WorkerCreateDto dto) {
        // 중복 체크
        if (workerRepository.findByWorkerCode(dto.getWorkerNo()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사번입니다: " + dto.getWorkerNo());
        }

        Worker worker = new Worker();
        worker.setWorkerCode(dto.getWorkerNo());
        worker.setWorkerName(dto.getName());
        worker.setIsActive(false);
        worker.setCreatedAt(LocalDateTime.now());

        // 초기 비밀번호 설정
        worker.setPassword(passwordEncoder.encode("1234"));

        // 직급 매핑
        if ("관리자".equals(dto.getPosition())) {
            worker.setRole(Role.ADMIN);
        } else {
            worker.setRole(Role.OPERATOR);
        }

        workerRepository.save(worker);
    }

    @Transactional
    public void updateWorker(Long workerId, WorkerUpdateDto dto) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("작업자 없음"));

        if (dto.getName() != null) worker.setWorkerName(dto.getName());
        if (dto.getActive() != null) worker.setIsActive(dto.getActive());

        if (dto.getPosition() != null) {
            if ("관리자".equals(dto.getPosition())) {
                worker.setRole(Role.ADMIN);
            } else {
                worker.setRole(Role.OPERATOR);
            }
        }
    }
}