package com.example.kreconomonmon.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EconomyIndicatorTest {

    @Test
    void createEconomyIndicator() {
        EconomyIndicator indicator = EconomyIndicator.builder()
                .statCode("722Y001")
                .itemCode("0101000")
                .period("202301")
                .value(new BigDecimal("3.5"))
                .build();

        assertThat(indicator.getStatCode()).isEqualTo("722Y001");
        assertThat(indicator.getItemCode()).isEqualTo("0101000");
        assertThat(indicator.getPeriod()).isEqualTo("202301");
        assertThat(indicator.getValue()).isEqualByComparingTo("3.5");
    }
}
