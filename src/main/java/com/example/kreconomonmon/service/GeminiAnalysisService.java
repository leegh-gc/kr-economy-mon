package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.AnalysisCache;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAnalysisService {

    private static final String ECONOMY_CACHE_KEY = "ECONOMY_ANALYSIS";
    private static final String REALESTATE_CACHE_KEY = "REALESTATE_ANALYSIS";
    private static final String ECONOMY_CARTOON_KEY = "ECONOMY_CARTOON";
    private static final String REALESTATE_CARTOON_KEY = "REALESTATE_CARTOON";

    private final EconomyIndicatorRepository economyIndicatorRepository;
    private final AnalysisCacheService cacheService;
    private final GeminiApiService geminiApiService;

    private final ReentrantLock economyLock = new ReentrantLock();
    private final ReentrantLock realestateLock = new ReentrantLock();
    private final ReentrantLock economyCartoonLock = new ReentrantLock();
    private final ReentrantLock realestateCartoonLock = new ReentrantLock();

    public Map<String, Object> getEconomyAnalysis() {
        String dataSnapshot = buildEconomyDataSnapshot();
        String dataHash = cacheService.computeHash(dataSnapshot);

        String cached = cacheService.getCachedText(ECONOMY_CACHE_KEY, dataHash);
        if (cached != null) {
            return Map.of("status", "ok", "text", cached, "cached", true);
        }

        if (!economyLock.tryLock()) {
            log.info("경제 분석 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(ECONOMY_CACHE_KEY);
            String fallbackText = fallback.map(AnalysisCache::getContentText)
                .orElse("경제 분석을 생성 중입니다. 잠시 후 다시 시도해주세요.");
            return Map.of("status", "ok", "text", fallbackText, "cached", true);
        }
        try {
            String prompt = buildEconomyPrompt(dataSnapshot);
            String analysisText = geminiApiService.generateText(prompt);
            cacheService.saveCache(ECONOMY_CACHE_KEY, ECONOMY_CACHE_KEY, analysisText, dataHash);
            return Map.of("status", "ok", "text", analysisText, "cached", false);
        } catch (Exception e) {
            log.error("경제 분석 생성 실패", e);
            return Map.of("status", "error", "text", "경제 분석을 불러올 수 없습니다.", "cached", false);
        } finally {
            economyLock.unlock();
        }
    }

    public Map<String, Object> getRealEstateAnalysis() {
        List<EconomyIndicator> kbTrade = economyIndicatorRepository
            .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y062", "P63ACA");
        List<EconomyIndicator> kbLease = economyIndicatorRepository
            .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y063", "P64ACA");

        String dataSnapshot = buildRealEstateDataSnapshot(kbTrade, kbLease);
        String dataHash = cacheService.computeHash(dataSnapshot);

        String cached = cacheService.getCachedText(REALESTATE_CACHE_KEY, dataHash);
        if (cached != null) {
            return Map.of("status", "ok", "text", cached, "cached", true);
        }

        if (!realestateLock.tryLock()) {
            log.info("부동산 분석 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(REALESTATE_CACHE_KEY);
            String fallbackText = fallback.map(AnalysisCache::getContentText)
                .orElse("부동산 분석을 생성 중입니다. 잠시 후 다시 시도해주세요.");
            return Map.of("status", "ok", "text", fallbackText, "cached", true);
        }
        try {
            String prompt = buildRealEstatePrompt(dataSnapshot);
            String analysisText = geminiApiService.generateText(prompt);
            cacheService.saveCache(REALESTATE_CACHE_KEY, REALESTATE_CACHE_KEY, analysisText, dataHash);
            return Map.of("status", "ok", "text", analysisText, "cached", false);
        } catch (Exception e) {
            log.error("부동산 분석 생성 실패", e);
            return Map.of("status", "error", "text", "부동산 분석을 불러올 수 없습니다.", "cached", false);
        } finally {
            realestateLock.unlock();
        }
    }

    public Map<String, Object> getEconomyCartoon() {
        String dataSnapshot = buildEconomyDataSnapshot();
        String dataHash = cacheService.computeHash(dataSnapshot);

        String cachedImage = cacheService.getCachedImage(ECONOMY_CARTOON_KEY, dataHash);
        if (cachedImage != null) {
            return Map.of("status", "ok", "imageData", cachedImage, "cached", true);
        }

        if (!economyCartoonLock.tryLock()) {
            log.info("경제 컷툰 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(ECONOMY_CARTOON_KEY);
            String fallbackImage = fallback.map(AnalysisCache::getImageData).orElse("");
            return Map.of("status", "ok", "imageData", fallbackImage != null ? fallbackImage : "", "cached", true);
        }
        try {
            String prompt = buildEconomyCartoonPrompt(dataSnapshot);
            String imageData = geminiApiService.generateImage(prompt);
            cacheService.saveOrUpdate(ECONOMY_CARTOON_KEY, ECONOMY_CARTOON_KEY, null, imageData, dataHash);
            return Map.of("status", "ok", "imageData", imageData, "cached", false);
        } catch (Exception e) {
            log.error("경제 컷툰 생성 실패", e);
            return Map.of("status", "error", "imageData", "", "cached", false);
        } finally {
            economyCartoonLock.unlock();
        }
    }

    public Map<String, Object> getRealEstateCartoon() {
        List<EconomyIndicator> kbTrade = economyIndicatorRepository
            .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y062", "P63ACA");
        List<EconomyIndicator> kbLease = economyIndicatorRepository
            .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y063", "P64ACA");

        String dataSnapshot = buildRealEstateDataSnapshot(kbTrade, kbLease);
        String dataHash = cacheService.computeHash(dataSnapshot);

        String cachedImage = cacheService.getCachedImage(REALESTATE_CARTOON_KEY, dataHash);
        if (cachedImage != null) {
            return Map.of("status", "ok", "imageData", cachedImage, "cached", true);
        }

        if (!realestateCartoonLock.tryLock()) {
            log.info("부동산 컷툰 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(REALESTATE_CARTOON_KEY);
            String fallbackImage = fallback.map(AnalysisCache::getImageData).orElse("");
            return Map.of("status", "ok", "imageData", fallbackImage != null ? fallbackImage : "", "cached", true);
        }
        try {
            String prompt = buildRealEstateCartoonPrompt(dataSnapshot);
            String imageData = geminiApiService.generateImage(prompt);
            cacheService.saveOrUpdate(REALESTATE_CARTOON_KEY, REALESTATE_CARTOON_KEY, null, imageData, dataHash);
            return Map.of("status", "ok", "imageData", imageData, "cached", false);
        } catch (Exception e) {
            log.error("부동산 컷툰 생성 실패", e);
            return Map.of("status", "error", "imageData", "", "cached", false);
        } finally {
            realestateCartoonLock.unlock();
        }
    }

    private String buildEconomyDataSnapshot() {
        StringBuilder sb = new StringBuilder();
        appendLatest(sb, "기준금리", "722Y001", "0101000");
        appendLatest(sb, "USD환율", "731Y001", "0000001");
        appendLatest(sb, "GDP성장률", "902Y015", "KOR");
        appendLatest(sb, "CPI", "901Y009", "0");
        return sb.toString();
    }

    private void appendLatest(StringBuilder sb, String label, String statCode, String itemCode) {
        List<EconomyIndicator> list = economyIndicatorRepository
            .findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode);
        if (!list.isEmpty()) {
            EconomyIndicator latest = list.get(list.size() - 1);
            sb.append(label).append("=").append(latest.getValue())
              .append("(").append(latest.getPeriod()).append("),");
        }
    }

    private String buildEconomyPrompt(String dataSnapshot) {
        return """
            당신은 한국 경제 전문가입니다. 아래 최신 경제지표 데이터를 기반으로 \
            현재 한국 경제 상황을 500자 이내로 분석해주세요. \
            수치를 직접 인용하고, 현재 추세와 시사점을 포함해주세요.

            [경제지표 데이터]
            %s
            """.formatted(dataSnapshot);
    }

    private String buildEconomyCartoonPrompt(String dataSnapshot) {
        return """
            아래 한국 경제지표 데이터를 바탕으로, 현재 경제 상황을 표현하는 \
            귀여운 2컷 만화(컷툰) 이미지를 생성해주세요. \
            밝고 유머러스한 스타일로, 경제 용어를 캐릭터 대화로 표현해주세요.

            [경제지표 데이터]
            %s
            """.formatted(dataSnapshot);
    }

    private String buildRealEstateDataSnapshot(
            List<EconomyIndicator> kbTrade, List<EconomyIndicator> kbLease) {
        StringBuilder sb = new StringBuilder();
        if (!kbTrade.isEmpty()) {
            EconomyIndicator latest = kbTrade.get(kbTrade.size() - 1);
            sb.append("KB매매지수=").append(latest.getValue())
              .append("(").append(latest.getPeriod()).append("),");
        }
        if (!kbLease.isEmpty()) {
            EconomyIndicator latest = kbLease.get(kbLease.size() - 1);
            sb.append("KB전세지수=").append(latest.getValue())
              .append("(").append(latest.getPeriod()).append("),");
        }
        return sb.toString();
    }

    private String buildRealEstatePrompt(String dataSnapshot) {
        return """
            당신은 서울 부동산 시장 전문가입니다. 아래 KB 아파트 지수 데이터를 기반으로 \
            현재 서울 부동산 시장 현황을 500자 이내로 분석해주세요. \
            지수 수치를 직접 인용하고, 매매/전세 시장의 추세와 시사점을 포함해주세요.

            [부동산 데이터]
            %s
            """.formatted(dataSnapshot);
    }

    private String buildRealEstateCartoonPrompt(String dataSnapshot) {
        return """
            아래 서울 아파트 KB 지수 데이터를 바탕으로, 현재 부동산 시장 상황을 표현하는 \
            귀여운 2컷 만화(컷툰) 이미지를 생성해주세요. \
            아파트 매매/전세 시장의 분위기를 유머러스하게 표현해주세요.

            [부동산 데이터]
            %s
            """.formatted(dataSnapshot);
    }
}
