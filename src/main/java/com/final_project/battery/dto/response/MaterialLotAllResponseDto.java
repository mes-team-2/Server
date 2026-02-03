package com.final_project.battery.dto.response;

import lombok.Getter;

@Getter
public class MaterialLotAllResponseDto {

    private Long total;    // 전체 LOT
    private Long running;  // AVAILABLE
    private Long waiting;  // HOLD
    private Long empty;    // EXHAUSTED

    public MaterialLotAllResponseDto(
            Long total,
            Long running,
            Long waiting,
            Long empty
    ) {
        this.total = total;
        this.running = running;
        this.waiting = waiting;
        this.empty = empty;
    }
}
