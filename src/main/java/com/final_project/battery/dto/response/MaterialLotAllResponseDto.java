package com.final_project.battery.dto.response;

import lombok.Getter;

@Getter
public class MaterialLotAllResponseDto {

    private Long total;    // 전체 LOT
    private Long waiting;  // AVAILABLE 대기중
    private Long running;  // HOLD 생산 중
    private Long empty;    // EXHAUSTED

    public MaterialLotAllResponseDto(
            Long total,
            Long waiting,
            Long running,
            Long empty
    ) {
        this.total = total;
        this.waiting = waiting;
        this.running = running;
        this.empty = empty;
    }
}
