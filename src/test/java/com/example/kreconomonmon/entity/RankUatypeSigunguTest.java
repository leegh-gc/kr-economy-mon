package com.example.kreconomonmon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class RankUatypeSigunguTest {

    @Test
    void builder_creates_entity_with_all_fields() {
        RankUatypeSigungu entity = RankUatypeSigungu.builder()
            .sigunguCode("11680")
            .useAreaType("UA04")
            .dealYear("2026")
            .rankType(0)
            .aptName("테스트아파트")
            .dongName("역삼동")
            .buildYear(2010)
            .avgPrice(new BigDecimal("200000"))
            .minPrice(new BigDecimal("180000"))
            .maxPrice(new BigDecimal("220000"))
            .dealCount(5)
            .build();

        assertThat(entity.getSigunguCode()).isEqualTo("11680");
        assertThat(entity.getAptName()).isEqualTo("테스트아파트");
        assertThat(entity.getAvgPrice()).isEqualByComparingTo("200000");
    }
}
