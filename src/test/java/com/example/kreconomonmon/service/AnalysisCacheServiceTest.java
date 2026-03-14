package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.AnalysisCache;
import com.example.kreconomonmon.repository.AnalysisCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisCacheServiceTest {

    @Mock
    private AnalysisCacheRepository repository;

    @InjectMocks
    private AnalysisCacheService cacheService;

    @Test
    void computeHash_same_input_gives_same_hash() {
        String hash1 = cacheService.computeHash("동일한 데이터");
        String hash2 = cacheService.computeHash("동일한 데이터");
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64);
    }

    @Test
    void computeHash_different_input_gives_different_hash() {
        String hash1 = cacheService.computeHash("데이터A");
        String hash2 = cacheService.computeHash("데이터B");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void getCachedText_returns_text_when_hash_matches() {
        AnalysisCache cached = AnalysisCache.builder()
            .cacheKey("ECONOMY_ANALYSIS")
            .cacheType("ECONOMY_ANALYSIS")
            .contentText("캐시된 분석 텍스트")
            .dataHash("matchinghash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(repository.findById("ECONOMY_ANALYSIS")).thenReturn(Optional.of(cached));

        String result = cacheService.getCachedText("ECONOMY_ANALYSIS", "matchinghash");

        assertThat(result).isEqualTo("캐시된 분석 텍스트");
    }

    @Test
    void getCachedText_returns_null_when_hash_differs() {
        AnalysisCache cached = AnalysisCache.builder()
            .cacheKey("ECONOMY_ANALYSIS")
            .cacheType("ECONOMY_ANALYSIS")
            .contentText("구버전 분석")
            .dataHash("oldhash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(repository.findById("ECONOMY_ANALYSIS")).thenReturn(Optional.of(cached));

        String result = cacheService.getCachedText("ECONOMY_ANALYSIS", "newhash");

        assertThat(result).isNull();
    }

    @Test
    void getCachedText_returns_null_when_no_cache_exists() {
        when(repository.findById("ECONOMY_ANALYSIS")).thenReturn(Optional.empty());

        String result = cacheService.getCachedText("ECONOMY_ANALYSIS", "anyhash");

        assertThat(result).isNull();
    }

    @Test
    void getCachedImage_returns_image_when_hash_matches() {
        AnalysisCache cached = AnalysisCache.builder()
            .cacheKey("ECONOMY_CARTOON")
            .cacheType("ECONOMY_CARTOON")
            .contentText("분석 텍스트")
            .imageData("base64imagedata==")
            .dataHash("matchinghash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(repository.findById("ECONOMY_CARTOON")).thenReturn(Optional.of(cached));

        String result = cacheService.getCachedImage("ECONOMY_CARTOON", "matchinghash");

        assertThat(result).isEqualTo("base64imagedata==");
    }

    @Test
    void getCachedImage_returns_null_when_hash_differs() {
        AnalysisCache cached = AnalysisCache.builder()
            .cacheKey("ECONOMY_CARTOON")
            .cacheType("ECONOMY_CARTOON")
            .imageData("oldimage==")
            .dataHash("oldhash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(repository.findById("ECONOMY_CARTOON")).thenReturn(Optional.of(cached));

        String result = cacheService.getCachedImage("ECONOMY_CARTOON", "newhash");

        assertThat(result).isNull();
    }

    @Test
    void saveCache_persists_new_entry() {
        when(repository.findById("ECONOMY_ANALYSIS")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cacheService.saveCache("ECONOMY_ANALYSIS", "ECONOMY_ANALYSIS", "분석 텍스트", "newhash");

        verify(repository, times(1)).save(any(AnalysisCache.class));
    }

    @Test
    void saveCache_updates_existing_entry() {
        AnalysisCache existing = AnalysisCache.builder()
            .cacheKey("ECONOMY_ANALYSIS")
            .cacheType("ECONOMY_ANALYSIS")
            .contentText("이전 분석")
            .dataHash("oldhash")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(repository.findById("ECONOMY_ANALYSIS")).thenReturn(Optional.of(existing));

        cacheService.saveCache("ECONOMY_ANALYSIS", "ECONOMY_ANALYSIS", "새 분석", "newhash");

        assertThat(existing.getContentText()).isEqualTo("새 분석");
        verify(repository, times(1)).save(existing);
    }

    @Test
    void saveOrUpdate_stores_image_data() {
        when(repository.findById("ECONOMY_CARTOON")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cacheService.saveOrUpdate("ECONOMY_CARTOON", "ECONOMY_CARTOON", "분석 텍스트", "base64img==", "newhash");

        verify(repository, times(1)).save(argThat(c ->
            "base64img==".equals(c.getImageData()) && "ECONOMY_CARTOON".equals(c.getCacheKey())
        ));
    }

    @Test
    void invalidate_deletes_by_key() {
        cacheService.invalidate("ECONOMY_ANALYSIS");
        verify(repository, times(1)).deleteById("ECONOMY_ANALYSIS");
    }

    @Test
    void invalidateAll_deletes_all() {
        cacheService.invalidateAll();
        verify(repository, times(1)).deleteAll();
    }
}
