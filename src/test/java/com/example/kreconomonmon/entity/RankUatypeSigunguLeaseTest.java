package com.example.kreconomonmon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class RankUatypeSigunguLeaseTest {

    @Test
    void builder_creates_entity_with_all_fields() {
        RankUatypeSigunguLease entity = RankUatypeSigunguLease.builder()
            .sigunguCode("11680")
            .useAreaType("UA04")
            .dealYear("2026")
            .rankType(0)
            .rentGbn("0")
            .aptName("전세아파트")
            .dongName("대치동")
            .buildYear(2015)
            .avgDeposit(new BigDecimal("150000"))
            .minDeposit(new BigDecimal("130000"))
            .maxDeposit(new BigDecimal("170000"))
            .dealCount(3)
            .build();

        assertThat(entity.getRentGbn()).isEqualTo("0");
        assertThat(entity.getAvgDeposit()).isEqualByComparingTo("150000");
    }
}
