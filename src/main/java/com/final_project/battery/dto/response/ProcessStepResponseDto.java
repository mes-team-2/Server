package com.final_project.battery.dto.response;

import com.final_project.battery.domain.ProcessStep;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessStepResponseDto {
    private Long processId;
    private Integer seq;
    private String processCode;
    private String processName;
    private Boolean active;

    public ProcessStepResponseDto(ProcessStep entity) {
        this.processId = entity.getProcessStepId();
        this.seq = entity.getSeq();
        this.processCode = entity.getStepCode();
        this.processName = entity.getStepName();
        this.active = entity.getActive();
    }
}