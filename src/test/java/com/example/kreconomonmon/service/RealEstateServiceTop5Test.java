package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.Top5Response;
import com.example.kreconomonmon.entity.RankUatypeSigungu;
import com.example.kreconomonmon.entity.RankUatypeSigunguLease;
import com.example.kreconomonmon.repository.RankUatypeSigunguLeaseRepository;
import com.example.kreconomonmon.repository.RankUatypeSigunguRepository;
import com.example.kreconomonmon.repository.StatLeaseSigunguRepository;
import com.example.kreconomonmon.repository.StatSigunguYymmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealEstateServiceTop5Test {

    @Mock
    private RankUatypeSigunguRepository tradeTop5Repository;

    @Mock
    private RankUatypeSigunguLeaseRepository leaseTop5Repository;

    @Mock
    private StatSigunguYymmRepository tradeRepository;

    @Mock
    private StatLeaseSigunguRepository leaseRepository;

    @InjectMocks
    private RealEstateService realEstateService;

    @Test
    void getTop5Trade_returns_list_of_5_rows() {
        when(tradeTop5Repository
            .findBySigunguCodeInAndUseAreaTypeAndDealYearAndRankTypeOrderByAvgPriceDesc(
                eq(List.of("11680")), eq("UA04"), anyString(), eq(0), any(Pageable.class)))
            .thenReturn(List.of(
                RankUatypeSigungu.builder()
                    .aptName("래미안").dongName("대치동").buildYear(2005)
                    .avgPrice(new BigDecimal("200000"))
                    .minPrice(new BigDecimal("180000"))
                    .maxPrice(new BigDecimal("220000"))
                    .dealCount(3)
                    .build()
            ));

        List<Top5Response> result = realEstateService.getTop5Trade(List.of("11680"), "UA04");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAptName()).isEqualTo("래미안");
        assertThat(result.get(0).getAvgPrice()).isEqualByComparingTo("200000");
    }

    @Test
    void getTop5Lease_returns_list_with_rent_gbn_zero() {
        when(leaseTop5Repository
            .findBySigunguCodeInAndUseAreaTypeAndDealYearAndRankTypeAndRentGbnOrderByAvgDepositDesc(
                eq(List.of("11680")), eq("UA04"), anyString(), eq(0), eq("0"), any(Pageable.class)))
            .thenReturn(List.of(
                RankUatypeSigunguLease.builder()
                    .aptName("은마").dongName("대치동").buildYear(1979)
                    .avgDeposit(new BigDecimal("90000"))
                    .minDeposit(new BigDecimal("80000"))
                    .maxDeposit(new BigDecimal("100000"))
                    .dealCount(2)
                    .build()
            ));

        List<Top5Response> result = realEstateService.getTop5Lease(List.of("11680"), "UA04");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAptName()).isEqualTo("은마");
    }
}
