package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessStepUpdateDto {
    private String processName; // 이름 수정 가능
    private Boolean active;     // 사용 여부 수정 가능
    // 코드는 보통 수정하지 않음 (Key 성격)
}