package com.final_project.battery.domain.common;

public enum DefectType {
    NONE("정상"),
    SCRATCH("찍힘/스크래치"),
    DIMENSION("치수 불량"),
    FOREIGN_MAT("이물질 오염"),
    VOLTAGE_LOW("전압 미달"),
    ETC("기타");

    private final String description;
    DefectType(String description) { this.description = description; }
}
