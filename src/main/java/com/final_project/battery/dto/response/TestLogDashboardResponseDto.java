package com.final_project.battery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TestLogDashboardResponseDto {
    // 카드
    private Long totalCount;
    private Long okCount;
    private Long ngCount;
    private Integer okRate;
    private String topDefectType;

    // 차트
    private List<Daily> daily;
    private List<Defect> defects;

    @Getter
    @AllArgsConstructor
    public static class Daily {
        private String day;
        private Long ok;
        private Long ng;
    }

    @Getter
    @AllArgsConstructor
    public static class Defect {
        private String defectType;
        private Long count;
    }
}
