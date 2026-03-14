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
     */
    @Transactional
    public void refreshFromApi(String statCode, String cycle, String itemCode) {
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode);
        if (rows.isEmpty()) {
            log.warn("ECOS API 데이터 없음: statCode={}, itemCode={}", statCode, itemCode);
            return;
        }

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
