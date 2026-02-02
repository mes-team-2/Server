package com.final_project.battery.service;

import com.final_project.battery.domain.ProcessStep;
import com.final_project.battery.dto.request.ProcessStepCreateDto;
import com.final_project.battery.dto.request.ProcessStepUpdateDto;
import com.final_project.battery.dto.response.ProcessStepResponseDto;
import com.final_project.battery.repository.ProcessStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessStepService {

    private final ProcessStepRepository processStepRepository;

    // 목록 조회 (순서대로 정렬)
    @Transactional(readOnly = true)
    public List<ProcessStepResponseDto> getAllProcesses() {
        return processStepRepository.findAll(Sort.by(Sort.Direction.ASC, "seq"))
                .stream()
                .map(ProcessStepResponseDto::new)
                .collect(Collectors.toList());
    }

    // 공정 생성
    @Transactional
    public void createProcess(ProcessStepCreateDto dto) {
        ProcessStep step = ProcessStep.builder()
                .seq(dto.getSeq())
                .stepCode(dto.getProcessCode())
                .stepName(dto.getProcessName())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        processStepRepository.save(step);
    }

    // 공정 수정
    @Transactional
    public void updateProcess(Long id, ProcessStepUpdateDto dto) {
        ProcessStep step = processStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공정 정보 없음"));

        if (dto.getProcessName() != null) step.setStepName(dto.getProcessName());
        if (dto.getActive() != null) step.setActive(dto.getActive());
    }
}