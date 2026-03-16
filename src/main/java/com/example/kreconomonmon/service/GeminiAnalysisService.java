package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.AnalysisCache;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DISPLAY_FMT =
        DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

    /** 분석 결과 Map 빌더 — analysedAt 포함 */
    private Map<String, Object> buildResult(String text, boolean cached, LocalDateTime analysedAt) {
        String at = (analysedAt != null) ? analysedAt.format(DISPLAY_FMT) : "";
        return Map.of("status", "ok", "text", text, "cached", cached, "analysedAt", at);
    }

    public Map<String, Object> getEconomyAnalysis() {
        // ① 오늘 캐시가 있으면 그대로 반환 (하루 1회 제한)
        Optional<AnalysisCache> todayCache = cacheService.findTodayCache(ECONOMY_CACHE_KEY);
        if (todayCache.isPresent()) {
            return buildResult(todayCache.get().getContentText(), true,
                               todayCache.get().getUpdatedAt());
        }

        // ② 동시 생성 방지
        if (!economyLock.tryLock()) {
            log.info("경제 분석 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(ECONOMY_CACHE_KEY);
            String fallbackText = fallback.map(AnalysisCache::getContentText)
                .orElse("경제 분석을 생성 중입니다. 잠시 후 다시 시도해주세요.");
            LocalDateTime fallbackAt = fallback.map(AnalysisCache::getUpdatedAt).orElse(null);
            return buildResult(fallbackText, true, fallbackAt);
        }
        try {
            String dataSnapshot = buildEconomyDataSnapshot();
            String dataHash = cacheService.computeHash(dataSnapshot);
            String analysisText = geminiApiService.generateText(buildEconomyPrompt(dataSnapshot));
            cacheService.saveCache(ECONOMY_CACHE_KEY, ECONOMY_CACHE_KEY, analysisText, dataHash);
            return buildResult(analysisText, false, LocalDateTime.now());
        } catch (Exception e) {
            log.error("경제 분석 생성 실패", e);
            return Map.of("status", "error", "text", "경제 분석을 불러올 수 없습니다.",
                          "cached", false, "analysedAt", "");
        } finally {
            economyLock.unlock();
        }
    }

    public Map<String, Object> getRealEstateAnalysis() {
        // ① 오늘 캐시가 있으면 그대로 반환 (하루 1회 제한)
        Optional<AnalysisCache> todayCache = cacheService.findTodayCache(REALESTATE_CACHE_KEY);
        if (todayCache.isPresent()) {
            return buildResult(todayCache.get().getContentText(), true,
                               todayCache.get().getUpdatedAt());
        }

        // ② 동시 생성 방지
        if (!realestateLock.tryLock()) {
            log.info("부동산 분석 생성 중 - 기존 캐시 사용 시도");
            Optional<AnalysisCache> fallback = cacheService.findByKey(REALESTATE_CACHE_KEY);
            String fallbackText = fallback.map(AnalysisCache::getContentText)
                .orElse("부동산 분석을 생성 중입니다. 잠시 후 다시 시도해주세요.");
            LocalDateTime fallbackAt = fallback.map(AnalysisCache::getUpdatedAt).orElse(null);
            return buildResult(fallbackText, true, fallbackAt);
        }
        try {
            List<EconomyIndicator> kbTrade = economyIndicatorRepository
                .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y062", "P63ACA");
            List<EconomyIndicator> kbLease = economyIndicatorRepository
                .findByStatCodeAndItemCodeOrderByPeriodAsc("901Y063", "P64ACA");
            String dataSnapshot = buildRealEstateDataSnapshot(kbTrade, kbLease);
            String dataHash = cacheService.computeHash(dataSnapshot);
            String analysisText = geminiApiService.generateText(buildRealEstatePrompt(dataSnapshot));
            cacheService.saveCache(REALESTATE_CACHE_KEY, REALESTATE_CACHE_KEY, analysisText, dataHash);
            return buildResult(analysisText, false, LocalDateTime.now());
        } catch (Exception e) {
            log.error("부동산 분석 생성 실패", e);
            return Map.of("status", "error", "text", "부동산 분석을 불러올 수 없습니다.",
                          "cached", false, "analysedAt", "");
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
            당신은 서울 및 수도권 부동산 시장 전문 애널리스트입니다. \
            아래 KB 아파트 지수 데이터를 기반으로 현재 시장을 다음 항목별로 상세히 분석해주세요. \
            각 항목은 한 줄 제목 뒤 구체적인 내용을 작성하며, 전체 1000자 내외로 작성해주세요.

            ① 현황 요약: KB 매매지수·전세지수 최신 수치를 직접 인용하고 전월/전년 대비 변화를 설명
            ② 매매 시장 분석: 상승/보합/하락 원인, 주요 권역별(강남·강북·경기) 동향
            ③ 전세 시장 분석: 전세 수급 상황, 역전세 리스크 또는 수요 증가 요인
            ④ 외부 변수 영향: 기준금리, 대출 규제, 입주 물량 등 부동산에 영향을 미치는 거시 요인
            ⑤ 향후 전망 및 시사점: 단기(3개월) 시장 방향과 실수요자·투자자 관점의 시사점

            [부동산 데이터]
            %s
            """.formatted(dataSnapshot);
    }

    private String buildRealEstateCartoonPrompt(String dataSnapshot) {
        return """
            아래 서울 아파트 KB 지수 데이터를 바탕으로, 현재 부동산 시장 상황을 표현하는 \
            신문 만평 스타일 이미지를 생성해주세요. \
            풍자적이고 위트 있는 시사 만평 형식으로, 아파트 매매·전세 시장의 현 분위기를 \
            캐릭터 대화와 상황 묘사로 표현해주세요. \
            한국 신문 만평 특유의 흑백 또는 담채색 펜화 스타일을 사용해주세요.

            [부동산 데이터]
            %s
            """.formatted(dataSnapshot);
    }

    public void invalidateRealEstateCache() {
        cacheService.invalidate(REALESTATE_CACHE_KEY);
        cacheService.invalidate(REALESTATE_CARTOON_KEY);
        log.info("부동산 분석 캐시 수동 초기화");
    }
}
