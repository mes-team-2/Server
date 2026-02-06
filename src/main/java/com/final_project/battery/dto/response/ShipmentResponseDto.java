package com.final_project.battery.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder

public class ShipmentResponseDto {
    private Long id;
    private LocalDateTime tx_time;
    private String tx_type;
    private String status_key;

    private String productCode;
    private String productName;

    private int qty;
    private String unit;
    private String location;
    private String note;

}
