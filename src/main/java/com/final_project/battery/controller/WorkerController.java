package com.final_project.battery.controller;

import com.final_project.battery.dto.request.WorkerCreateDto;
import com.final_project.battery.dto.request.WorkerUpdateDto;
import com.final_project.battery.dto.response.WorkerDetailDto;
import com.final_project.battery.dto.response.WorkerResponseDto;
import com.final_project.battery.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    public ResponseEntity<List<WorkerResponseDto>> getWorkerList() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @PostMapping
    public ResponseEntity<String> createWorker(@RequestBody WorkerCreateDto dto) {
        workerService.createWorker(dto);
        return ResponseEntity.ok("작업자가 등록되었습니다.");
    }

    @PutMapping("/{workerId}")
    public ResponseEntity<String> updateWorker(@PathVariable Long workerId, @RequestBody WorkerUpdateDto dto) {
        workerService.updateWorker(workerId, dto);
        return ResponseEntity.ok("작업자 정보가 수정되었습니다.");
    }

    @GetMapping("/{workerId}/detail")
    public ResponseEntity<WorkerDetailDto> getWorkerDetail(@PathVariable Long workerId) {
        return ResponseEntity.ok(workerService.getWorkerDetail(workerId));
    }
}