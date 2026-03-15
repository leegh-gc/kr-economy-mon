package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.AnalysisCache;
import com.example.kreconomonmon.repository.AnalysisCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCacheService {

    private final AnalysisCacheRepository repository;

    public String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    public Optional<AnalysisCache> findByKey(String cacheKey) {
        return repository.findById(cacheKey);
    }

    public String getCachedText(String cacheKey, String currentDataHash) {
        Optional<AnalysisCache> cached = findByKey(cacheKey);
        if (cached.isEmpty()) {
            log.debug("캐시 없음: cacheKey={}", cacheKey);
            return null;
        }
        AnalysisCache cache = cached.get();
        if (!currentDataHash.equals(cache.getDataHash())) {
            log.debug("캐시 해시 불일치: cacheKey={}, stored={}, current={}",
                cacheKey, cache.getDataHash(), currentDataHash);
            return null;
        }
        log.debug("캐시 히트: cacheKey={}", cacheKey);
        return cache.getContentText();
    }

    public String getCachedImage(String cacheKey, String currentDataHash) {
        Optional<AnalysisCache> cached = findByKey(cacheKey);
        if (cached.isEmpty()) {
            return null;
        }
        AnalysisCache cache = cached.get();
        if (!currentDataHash.equals(cache.getDataHash())) {
            return null;
        }
        return cache.getImageData();
    }

    @Transactional
    public void saveCache(String cacheKey, String cacheType, String contentText, String dataHash) {
        saveOrUpdate(cacheKey, cacheType, contentText, null, dataHash);
    }

    @Transactional
    public void saveOrUpdate(String cacheKey, String cacheType, String contentText, String imageData, String dataHash) {
        Optional<AnalysisCache> existing = findByKey(cacheKey);
        if (existing.isPresent()) {
            existing.get().updateContent(contentText, imageData, dataHash);
            repository.save(existing.get());
            log.info("캐시 갱신: cacheKey={}", cacheKey);
        } else {
            AnalysisCache newCache = AnalysisCache.builder()
                .cacheKey(cacheKey)
                .cacheType(cacheType)
                .contentText(contentText)
                .imageData(imageData)
                .dataHash(dataHash)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.save(newCache);
            log.info("캐시 신규 저장: cacheKey={}", cacheKey);
        }
    }

    @Transactional
    public void invalidate(String cacheKey) {
        repository.deleteById(cacheKey);
        log.info("캐시 무효화: cacheKey={}", cacheKey);
    }

    @Transactional
    public void invalidateAll() {
        repository.deleteAll();
        log.info("전체 캐시 무효화");
    }
}
