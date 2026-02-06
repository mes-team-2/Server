package com.final_project.battery.dto.request;

import com.final_project.battery.domain.common.ShipmentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ShipmentCreateRequestDto {
    private ShipmentType txType;   // PRODUCTION_IN / SHIPMENT_OUT / ADJUSTMENT
    private int qty;               // + / -
    private String location;
    private String note;

    // 제품 스냅샷
    private String productCode;
    private String productName;
    private String unit;
}
