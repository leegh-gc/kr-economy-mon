package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomyIndicatorService {

    private final EconomyIndicatorRepository repository;
    private final EcosApiService ecosApiService;

    /**
     * DB에 데이터가 있으면 DB에서, 없으면 ECOS API에서 조회 후 저장하여 반환.
     */
    @Transactional
    public List<EconomyIndicator> getIndicators(String statCode, String cycle, String itemCode) {
        List<EconomyIndicator> dbData =
                repository.findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode);

        if (!dbData.isEmpty()) {
            log.debug("DB 캐시 히트: statCode={}, itemCode={}, count={}", statCode, itemCode, dbData.size());
            return dbData;
        }

        log.info("DB 캐시 없음, ECOS API 호출: statCode={}, itemCode={}", statCode, itemCode);
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode);

        List<EconomyIndicator> saved = rows.stream()
                .filter(row -> row.getDataValue() != null && !row.getDataValue().isBlank())
                .map(row -> {
                    BigDecimal value;
                    try {
                        value = new BigDecimal(row.getDataValue());
                    } catch (NumberFormatException e) {
                        log.warn("숫자 변환 실패: period={}, value={}", row.getTime(), row.getDataValue());
                        return null;
                    }
                    return EconomyIndicator.builder()
                            .statCode(statCode)
                            .itemCode(itemCode)
                            .period(row.getTime())
                            .value(value)
                            .build();
                })
                .filter(e -> e != null)
                .map(repository::save)
                .toList();

        return saved;
    }

    /**
     * ECOS API를 직접 호출하여 누락 데이터를 upsert한다.
     * 스케줄러가 매일 1회 호출 — DB 선조회 없이 항상 API 호출.
     * @Transactional 없음: ECOS API 호출(최대 45초) 동안 DB 커넥션을 잡지 않기 위해.
     */
    public void refreshFromApi(String statCode, String cycle, String itemCode) {
        // Phase 1: ECOS API 호출 (DB 커넥션 불필요)
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode);
        if (rows.isEmpty()) {
            log.warn("ECOS API 데이터 없음: statCode={}, itemCode={}", statCode, itemCode);
            return;
        }

        // Phase 2: DB upsert (각 save가 자체 트랜잭션 사용)
        Map<String, EconomyIndicator> existingByPeriod = repository
                .findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode)
                .stream()
                .collect(Collectors.toMap(EconomyIndicator::getPeriod, Function.identity()));

        int inserted = 0, updated = 0;
        for (EcosApiResponse.Row row : rows) {
            if (row.getDataValue() == null || row.getDataValue().isBlank()) continue;
            BigDecimal value;
            try {
                value = new BigDecimal(row.getDataValue());
            } catch (NumberFormatException e) {
                log.warn("숫자 변환 실패: period={}, value={}", row.getTime(), row.getDataValue());
                continue;
            }

            EconomyIndicator existing = existingByPeriod.get(row.getTime());
            if (existing != null) {
                existing.updateValue(value);
                repository.save(existing);  // 명시적 save (detached 엔티티 merge)
                updated++;
            } else {
                repository.save(EconomyIndicator.builder()
                        .statCode(statCode)
                        .itemCode(itemCode)
                        .period(row.getTime())
                        .value(value)
                        .build());
                inserted++;
            }
        }
        log.info("ECOS 갱신 완료: statCode={}, itemCode={}, inserted={}, updated={}",
                statCode, itemCode, inserted, updated);
    }

    /**
     * itemCode가 두 개인 통계 (예: 731Y004)를 한 번의 API 호출로 가져와
     * 각 itemCode별로 DB에 저장하고, Map<itemCode, List> 형태로 반환.
     */
    @Transactional
    public Map<String, List<EconomyIndicator>> getIndicatorsPair(
            String statCode, String cycle, String itemCode1, String itemCode2) {

        List<EconomyIndicator> db1 = repository.findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode1);
        List<EconomyIndicator> db2 = repository.findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode2);

        if (!db1.isEmpty() && !db2.isEmpty()) {
            return Map.of(itemCode1, db1, itemCode2, db2);
        }

        log.info("DB 캐시 없음, ECOS API 호출 (pair): statCode={}, {}+{}", statCode, itemCode1, itemCode2);
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode1, itemCode2);

        // DB에 이미 있는 코드는 저장 스킵, 없는 코드만 INSERT
        boolean needSave1 = db1.isEmpty();
        boolean needSave2 = db2.isEmpty();

        Map<String, List<EconomyIndicator>> result = new java.util.HashMap<>();
        result.put(itemCode1, needSave1 ? new java.util.ArrayList<>() : db1);
        result.put(itemCode2, needSave2 ? new java.util.ArrayList<>() : db2);

        for (EcosApiResponse.Row row : rows) {
            if (row.getDataValue() == null || row.getDataValue().isBlank()) continue;
            String code = row.getItemCode1();
            if (code.equals(itemCode1) && !needSave1) continue;
            if (code.equals(itemCode2) && !needSave2) continue;
            if (!result.containsKey(code)) continue;
            try {
                EconomyIndicator entity = EconomyIndicator.builder()
                        .statCode(statCode).itemCode(code)
                        .period(row.getTime())
                        .value(new BigDecimal(row.getDataValue()))
                        .build();
                result.get(code).add(repository.save(entity));
            } catch (NumberFormatException e) {
                log.warn("숫자 변환 실패: period={}, value={}", row.getTime(), row.getDataValue());
            }
        }
        return result;
    }

    /**
     * 스케줄러용 — itemCode 두 개짜리 통계를 한 번 API 호출로 upsert.
     */
    public void refreshFromApiPair(String statCode, String cycle, String itemCode1, String itemCode2) {
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode1, itemCode2);
        if (rows.isEmpty()) {
            log.warn("ECOS API 데이터 없음 (pair): statCode={}, {}+{}", statCode, itemCode1, itemCode2);
            return;
        }

        for (String itemCode : List.of(itemCode1, itemCode2)) {
            Map<String, EconomyIndicator> existing = repository
                    .findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode)
                    .stream().collect(Collectors.toMap(EconomyIndicator::getPeriod, Function.identity()));

            int inserted = 0, updated = 0;
            for (EcosApiResponse.Row row : rows) {
                if (!itemCode.equals(row.getItemCode1())) continue;
                if (row.getDataValue() == null || row.getDataValue().isBlank()) continue;
                try {
                    BigDecimal value = new BigDecimal(row.getDataValue());
                    EconomyIndicator e = existing.get(row.getTime());
                    if (e != null) { e.updateValue(value); repository.save(e); updated++; }
                    else { repository.save(EconomyIndicator.builder()
                            .statCode(statCode).itemCode(itemCode)
                            .period(row.getTime()).value(value).build()); inserted++; }
                } catch (NumberFormatException ex) {
                    log.warn("숫자 변환 실패: period={}", row.getTime());
                }
            }
            log.info("ECOS 갱신 완료(pair): statCode={}, itemCode={}, inserted={}, updated={}",
                    statCode, itemCode, inserted, updated);
        }
    }

    /**
     * 데이터 포인트가 maxPoints를 초과하면 균등 샘플링하여 반환.
     * Chart.js 렌더링 성능 최적화용.
     */
    public List<EconomyIndicator> sampleIfNeeded(List<EconomyIndicator> data, int maxPoints) {
        if (data.size() <= maxPoints) {
            return data;
        }
        double step = (double) data.size() / maxPoints;
        List<EconomyIndicator> sampled = new java.util.ArrayList<>();
        for (int i = 0; i < maxPoints; i++) {
            int index = (int) Math.round(i * step);
            if (index >= data.size()) index = data.size() - 1;
            sampled.add(data.get(index));
        }
        return sampled;
    }
}
