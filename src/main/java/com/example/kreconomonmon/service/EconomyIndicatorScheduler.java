package com.example.kreconomonmon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 경제 지표 일일 갱신 스케줄러.
 * 매일 새벽 1시에 ECOS API를 호출하여 economy_indicator 테이블에 누락 데이터를 upsert.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EconomyIndicatorScheduler {

    private final EconomyIndicatorService economyIndicatorService;

    /** statCode, cycle, itemCode1, itemCode2 — URL에 itemCode 두 개가 필요한 통계 */
    @SuppressWarnings("unchecked")
    private static final List<String[]> PAIRED_INDICATORS = List.<String[]>of(
            // 환율 (월평균, 731Y004는 두 항목을 동시에 조회해야 데이터 반환)
            new String[]{"731Y004", "M", "0000001", "0000100"},
            // 고용 (901Y027도 두 항목 동시 조회 필요 — 단독 조회 시 중복 키 발생)
            new String[]{"901Y027", "M", "I61BC", "I28B"},
            new String[]{"901Y027", "M", "I61BA", "I28B"}
    );

    /** statCode, cycle, itemCode 조합 — EconomyApiController 8개 섹션 전체 */
    private static final List<String[]> INDICATORS = List.of(
            // 금리
            new String[]{"722Y001", "M", "0101000"},
            new String[]{"817Y002", "D", "010200000"},
            new String[]{"817Y002", "D", "010210000"},
            // GDP / 성장률
            new String[]{"902Y015", "Q", "KOR"},
            new String[]{"902Y018", "A", "KOR"},
            // 물가
            new String[]{"901Y009", "M", "0"},
            new String[]{"404Y014", "M", "*AA"},
            // 무역 / 경상수지
            new String[]{"301Y013", "M", "000000"},
            new String[]{"901Y118", "M", "T002"},
            new String[]{"901Y118", "M", "T004"},
            // 통화 / 유동성
            new String[]{"902Y005", "M", "KR"},
            new String[]{"902Y014", "M", "KR"},
            new String[]{"902Y014", "M", "JP"},
            new String[]{"902Y014", "M", "CN"},
            // 인구 / 출산율 / 고령화
            new String[]{"901Y028", "A", "I35A"},
            new String[]{"901Y028", "A", "I35B"},
            new String[]{"901Y028", "A", "I35C"},
            new String[]{"901Y028", "A", "I35D"},
            new String[]{"901Y028", "A", "I35E"},
            // KB 부동산 지수 (서울 아파트 매매/전세)
            new String[]{"901Y062", "M", "P63ACA"},
            new String[]{"901Y063", "M", "P64ACA"}
    );

    /** 매일 새벽 1시 실행 */
    @Scheduled(cron = "0 0 1 * * *")
    public void refreshAllIndicators() {
        int total = INDICATORS.size() + PAIRED_INDICATORS.size();
        log.info("=== 경제 지표 일일 갱신 시작 ({} 건) ===", total);
        int success = 0, failed = 0;

        for (String[] ind : INDICATORS) {
            try {
                economyIndicatorService.refreshFromApi(ind[0], ind[1], ind[2]);
                success++;
            } catch (Exception e) {
                log.error("지표 갱신 실패: statCode={}, itemCode={}", ind[0], ind[2], e);
                failed++;
            }
        }

        for (String[] ind : PAIRED_INDICATORS) {
            try {
                economyIndicatorService.refreshFromApiPair(ind[0], ind[1], ind[2], ind[3]);
                success++;
            } catch (Exception e) {
                log.error("지표 갱신 실패(pair): statCode={}, itemCode1={}, itemCode2={}", ind[0], ind[2], ind[3], e);
                failed++;
            }
        }

        log.info("=== 경제 지표 일일 갱신 완료 — 성공: {}, 실패: {} ===", success, failed);
    }
}
