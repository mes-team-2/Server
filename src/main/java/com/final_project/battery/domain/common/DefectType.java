package com.final_project.battery.domain.common;

public enum DefectType {
    // 1. 전극 공정 (MAC-A-01)
    SCRATCH,            // 스크래치
    THICKNESS_ERROR,    // 두께 불량

    // 2. 조립 공정 (MAC-A-02)
    MISALIGNMENT,       // 정렬 불량
    MISSING_PART,       // 부품 누락

    // 3. 활성화 공정 (MAC-A-03)
    LOW_VOLTAGE,        // 전압 미달
    HIGH_TEMP,          // 고온 발생

    // 4. 팩 공정 (MAC-A-04)
    WELDING_ERROR,      // 용접 불량
    LABEL_ERROR,        // 라벨 부착 불량

    // 5. 검사 공정 (MAC-A-05)
    DIMENSION_ERROR,    // 치수 불량
    FOREIGN_MATERIAL,   // 이물질 혼입

    // 공통/기타
    ETC,
    NONE // 양품
}
