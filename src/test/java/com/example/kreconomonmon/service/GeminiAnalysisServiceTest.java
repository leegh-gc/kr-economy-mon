package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.AnalysisCache;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAnalysisServiceTest {

    @Mock
    private EconomyIndicatorRepository economyIndicatorRepository;

    @Mock
    private AnalysisCacheService cacheService;

    @Mock
    private GeminiApiService geminiApiService;

    @InjectMocks
    private GeminiAnalysisService analysisService;

    /** 테스트용 캐시 엔티티 빌더 */
    private AnalysisCache buildCache(String key, String text) {
        return AnalysisCache.builder()
            .cacheKey(key).cacheType(key)
            .contentText(text).dataHash("testhash")
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void getEconomyAnalysis_returns_cached_when_today_cache_exists() {
        when(cacheService.findTodayCache("ECONOMY_ANALYSIS"))
            .thenReturn(Optional.of(buildCache("ECONOMY_ANALYSIS", "캐시된 경제 분석")));

        Map<String, Object> result = analysisService.getEconomyAnalysis();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("text")).isEqualTo("캐시된 경제 분석");
        assertThat(result.get("cached")).isEqualTo(true);
        verify(geminiApiService, never()).generateText(anyString());
    }

    @Test
    void getEconomyAnalysis_calls_gemini_when_cache_miss() {
        when(cacheService.findTodayCache("ECONOMY_ANALYSIS")).thenReturn(Optional.empty());
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(anyString(), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("newhash");
        when(geminiApiService.generateText(anyString())).thenReturn("새로운 분석 텍스트");

        Map<String, Object> result = analysisService.getEconomyAnalysis();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("text")).isEqualTo("새로운 분석 텍스트");
        assertThat(result.get("cached")).isEqualTo(false);
        verify(cacheService).saveCache(eq("ECONOMY_ANALYSIS"), eq("ECONOMY_ANALYSIS"), eq("새로운 분석 텍스트"), eq("newhash"));
    }

    @Test
    void getEconomyAnalysis_returns_error_on_exception() {
        when(cacheService.findTodayCache("ECONOMY_ANALYSIS")).thenReturn(Optional.empty());
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(anyString(), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("newhash");
        when(geminiApiService.generateText(anyString())).thenThrow(new RuntimeException("API 오류"));

        Map<String, Object> result = analysisService.getEconomyAnalysis();

        assertThat(result.get("status")).isEqualTo("error");
        assertThat(result.get("text").toString()).contains("불러올 수 없습니다");
    }

    @Test
    void getRealEstateAnalysis_returns_cached_when_today_cache_exists() {
        when(cacheService.findTodayCache("REALESTATE_ANALYSIS"))
            .thenReturn(Optional.of(buildCache("REALESTATE_ANALYSIS", "캐시된 부동산 분석")));

        Map<String, Object> result = analysisService.getRealEstateAnalysis();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("text")).isEqualTo("캐시된 부동산 분석");
        assertThat(result.get("cached")).isEqualTo(true);
    }

    @Test
    void getEconomyCartoon_returns_cached_image_when_hash_matches() {
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(anyString(), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("testhash");
        when(cacheService.getCachedImage("ECONOMY_CARTOON", "testhash")).thenReturn("base64img==");

        Map<String, Object> result = analysisService.getEconomyCartoon();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("imageData")).isEqualTo("base64img==");
        assertThat(result.get("cached")).isEqualTo(true);
        verify(geminiApiService, never()).generateImage(anyString());
    }

    @Test
    void getEconomyCartoon_calls_gemini_image_on_cache_miss() {
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(anyString(), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("newhash");
        when(cacheService.getCachedImage("ECONOMY_CARTOON", "newhash")).thenReturn(null);
        when(geminiApiService.generateImage(anyString())).thenReturn("newbase64img==");

        Map<String, Object> result = analysisService.getEconomyCartoon();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("imageData")).isEqualTo("newbase64img==");
        assertThat(result.get("cached")).isEqualTo(false);
        verify(cacheService).saveOrUpdate(eq("ECONOMY_CARTOON"), eq("ECONOMY_CARTOON"),
            isNull(), eq("newbase64img=="), eq("newhash"));
    }

    @Test
    void getEconomyCartoon_returns_error_on_exception() {
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(anyString(), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("newhash");
        when(cacheService.getCachedImage(anyString(), anyString())).thenReturn(null);
        when(geminiApiService.generateImage(anyString())).thenThrow(new RuntimeException("이미지 API 오류"));

        Map<String, Object> result = analysisService.getEconomyCartoon();

        assertThat(result.get("status")).isEqualTo("error");
        assertThat(result.get("imageData")).isEqualTo("");
    }

    @Test
    void getRealEstateCartoon_returns_cached_image_when_hash_matches() {
        EconomyIndicator kbTrade = EconomyIndicator.builder()
            .statCode("901Y062").itemCode("P63ACA").period("202502")
            .value(new BigDecimal("100.5")).updatedAt(LocalDateTime.now())
            .build();
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc("901Y062", "P63ACA"))
            .thenReturn(List.of(kbTrade));
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc("901Y063", "P64ACA"))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("testhash");
        when(cacheService.getCachedImage("REALESTATE_CARTOON", "testhash")).thenReturn("rebase64==");

        Map<String, Object> result = analysisService.getRealEstateCartoon();

        assertThat(result.get("status")).isEqualTo("ok");
        assertThat(result.get("imageData")).isEqualTo("rebase64==");
    }

    @Test
    void getEconomyAnalysis_includes_latest_indicator_in_snapshot() {
        when(cacheService.findTodayCache("ECONOMY_ANALYSIS")).thenReturn(Optional.empty());
        EconomyIndicator indicator = EconomyIndicator.builder()
            .statCode("722Y001").itemCode("0101000").period("202502")
            .value(new BigDecimal("3.50")).updatedAt(LocalDateTime.now())
            .build();
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc("722Y001", "0101000"))
            .thenReturn(List.of(indicator));
        when(economyIndicatorRepository.findByStatCodeAndItemCodeOrderByPeriodAsc(argThat(s -> !s.equals("722Y001")), anyString()))
            .thenReturn(List.of());
        when(cacheService.computeHash(anyString())).thenReturn("h");
        when(geminiApiService.generateText(anyString())).thenReturn("분석결과");

        analysisService.getEconomyAnalysis();

        verify(geminiApiService).generateText(argThat(prompt -> prompt.contains("기준금리")));
    }
}
