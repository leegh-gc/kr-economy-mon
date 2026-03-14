package com.example.kreconomonmon.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class AnalysisCacheTest {

    @Test
    void builder_creates_entity() {
        LocalDateTime now = LocalDateTime.now();
        AnalysisCache cache = AnalysisCache.builder()
            .cacheKey("ECONOMY_ANALYSIS")
            .cacheType("ECONOMY_ANALYSIS")
            .contentText("한국 경제는 현재 안정적인 성장세를 유지하고 있습니다.")
            .dataHash("abc123def456")
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertThat(cache.getCacheKey()).isEqualTo("ECONOMY_ANALYSIS");
        assertThat(cache.getDataHash()).isEqualTo("abc123def456");
        assertThat(cache.getContentText()).contains("성장세");
    }

    @Test
    void updateContent_changes_text_and_hash() {
        AnalysisCache cache = AnalysisCache.builder()
            .cacheKey("ECONOMY_ANALYSIS")
            .cacheType("ECONOMY_ANALYSIS")
            .contentText("이전 분석")
            .dataHash("oldhash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        cache.updateContent("새로운 분석 텍스트", "newhash");

        assertThat(cache.getContentText()).isEqualTo("새로운 분석 텍스트");
        assertThat(cache.getDataHash()).isEqualTo("newhash");
    }
}
