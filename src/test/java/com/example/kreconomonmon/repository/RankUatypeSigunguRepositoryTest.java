package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.RankUatypeSigungu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankUatypeSigunguRepositoryTest {

    @Mock
    private RankUatypeSigunguRepository repository;

    @Test
    void findTop5_returns_correct_results() {
        List<RankUatypeSigungu> mockData = List.of(
            RankUatypeSigungu.builder().sigunguCode("11680").useAreaType("UA04").dealYear("2026")
                .rankType(0).aptName("아파트A").avgPrice(new BigDecimal("60000")).dealCount(3).build(),
            RankUatypeSigungu.builder().sigunguCode("11680").useAreaType("UA04").dealYear("2026")
                .rankType(0).aptName("아파트B").avgPrice(new BigDecimal("50000")).dealCount(2).build()
        );

        when(repository.findBySigunguCodeAndUseAreaTypeAndDealYearAndRankTypeOrderByAvgPriceDesc(
            eq("11680"), eq("UA04"), eq("2026"), eq(0), any()))
            .thenReturn(mockData);

        List<RankUatypeSigungu> result = repository
            .findBySigunguCodeAndUseAreaTypeAndDealYearAndRankTypeOrderByAvgPriceDesc(
                "11680", "UA04", "2026", 0, PageRequest.of(0, 5));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAptName()).isEqualTo("아파트A");
    }
}
