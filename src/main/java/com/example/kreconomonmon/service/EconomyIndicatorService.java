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
}
