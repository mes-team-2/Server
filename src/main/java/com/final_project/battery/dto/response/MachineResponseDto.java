package com.final_project.battery.dto.response;

import com.final_project.battery.domain.Machine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineResponseDto {
    private Long machineId;
    private String machineCode;
    private String machineName;
    private String processCode;
    private String status;      // RUN, WAIT, STOP, ERROR
    private String active;      // YES / NO
    private String errorLog;    // 에러 메시지 (화면용)

    public MachineResponseDto(Machine m) {
        this.machineId = m.getMachineId();
        this.machineCode = m.getMachineCode();
        this.machineName = m.getMachineName();
        this.processCode = m.getProcessCode();
        this.status = m.getStatus().name();
        this.active = (m.getIsActive() != null && m.getIsActive()) ? "YES" : "NO";

        // [Natural Hardcoding] 상태가 ERROR면 그럴듯한 메시지 생성
        if ("ERROR".equals(this.status)) {
            this.errorLog = "센서 응답 시간 초과 (Code: 503)";
        } else {
            this.errorLog = "-";
        }
    }
}