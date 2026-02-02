package com.final_project.battery.controller;

import com.final_project.battery.dto.request.ProcessStepCreateDto;
import com.final_project.battery.dto.request.ProcessStepUpdateDto;
import com.final_project.battery.dto.response.ProcessStepResponseDto;
import com.final_project.battery.service.ProcessStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/processes")
@RequiredArgsConstructor
public class ProcessStepController {

    private final ProcessStepService processStepService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<ProcessStepResponseDto>> getProcessList() {
        return ResponseEntity.ok(processStepService.getAllProcesses());
    }

    // 등록
    @PostMapping
    public ResponseEntity<String> createProcess(@RequestBody ProcessStepCreateDto dto) {
        processStepService.createProcess(dto);
        return ResponseEntity.ok("공정이 등록되었습니다.");
    }

    // 수정
    @PutMapping("/{processId}")
    public ResponseEntity<String> updateProcess(@PathVariable Long processId, @RequestBody ProcessStepUpdateDto dto) {
        processStepService.updateProcess(processId, dto);
        return ResponseEntity.ok("공정 정보가 수정되었습니다.");
    }
}