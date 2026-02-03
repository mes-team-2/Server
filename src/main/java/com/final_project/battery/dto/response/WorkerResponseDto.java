package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Worker;
import com.final_project.battery.domain.common.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerResponseDto {
    private Long id;          // Front: id (Row Key)
    private String workerNo;  // Front: workerNo
    private String name;      // Front: name
    private String position;  // Front: position ("작업자", "관리자")
    private String joinedAt;  // Front: joinedAt
    private Boolean active;   // Front: active

    public WorkerResponseDto(Worker entity) {
        this.id = entity.getWorkerId();
        this.workerNo = entity.getWorkerCode();
        this.name = entity.getWorkerName();
        this.active = entity.getIsActive();

        // Role Enum -> 한글 직급 변환
        if (entity.getRole() == Role.ADMIN) {
            this.position = "관리자";
        } else {
            this.position = "작업자"; // 기본값
        }

        if (entity.getCreatedAt() != null) {
            this.joinedAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            this.joinedAt = "-";
        }
    }
}