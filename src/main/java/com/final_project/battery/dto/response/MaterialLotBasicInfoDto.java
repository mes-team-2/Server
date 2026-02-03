package com.final_project.battery.dto.response;

import com.final_project.battery.domain.common.MaterialLotStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaterialLotBasicInfoDto(
        Long materialLotId,
        String materialLotNo,
        MaterialLotStatus status,
        LocalDateTime inputDate,
        String materialCode,
        String materialName,
        String unit,
        BigDecimal remainQty
) {}

