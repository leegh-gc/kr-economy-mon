package com.example.kreconomonmon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Chart.js 렌더링용 공통 응답 DTO.
 * labels: X축 레이블 목록 (기간 문자열)
 * datasets: 시리즈별 데이터셋 목록
 */
@Getter
@Builder
public class ChartDataResponse {

    private List<String> labels;
    private List<Dataset> datasets;

    @Getter
    @Builder
    public static class Dataset {
        private String label;           // 범례 이름 (예: "기준금리")
        private List<Double> data;      // Y축 데이터 포인트
        private String borderColor;     // 선 색상 (예: "#007bff")
        private String backgroundColor; // 채우기 색상 (투명도 포함)
        private boolean fill;           // 영역 채우기 여부
        private String yAxisID;         // 이중 Y축 사용 시 축 ID (예: "y", "y1")
    }
}
