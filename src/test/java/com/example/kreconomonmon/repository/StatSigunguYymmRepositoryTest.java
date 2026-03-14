package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.StatSigunguYymm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatSigunguYymmRepositoryTest {

    @Mock
    private StatSigunguYymmRepository repository;

    @Test
    void findByCodesAndAreaType_returnsList() {
        List<String> codes = List.of("11680", "11650");
        StatSigunguYymm mockData = StatSigunguYymm.builder()
                .sigunguCode("11680")
                .useAreaType("UA04")
                .dealYymm("202301")
                .avgPrice(new BigDecimal("150000"))
                .build();

        when(repository.findByCodesAndAreaType(codes, "UA04"))
                .thenReturn(List.of(mockData));

        List<StatSigunguYymm> result = repository.findByCodesAndAreaType(codes, "UA04");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSigunguCode()).isEqualTo("11680");
    }
}
