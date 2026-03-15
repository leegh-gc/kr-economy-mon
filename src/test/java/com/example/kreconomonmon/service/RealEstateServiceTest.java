package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.entity.StatSigunguYymm;
import com.example.kreconomonmon.repository.StatLeaseSigunguRepository;
import com.example.kreconomonmon.repository.StatSigunguYymmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealEstateServiceTest {

    @Mock
    private StatSigunguYymmRepository tradeRepository;

    @Mock
    private StatLeaseSigunguRepository leaseRepository;

    @InjectMocks
    private RealEstateService realEstateService;

    @Test
    void getPriceChartData_returnsDatasetsPerDistrict() {
        List<StatSigunguYymm> mockData = List.of(
            StatSigunguYymm.builder()
                .sigunguCode("11680").dealYymm("202301").avgPrice(new BigDecimal("150000")).build(),
            StatSigunguYymm.builder()
                .sigunguCode("11680").dealYymm("202302").avgPrice(new BigDecimal("151000")).build(),
            StatSigunguYymm.builder()
                .sigunguCode("11650").dealYymm("202301").avgPrice(new BigDecimal("130000")).build()
        );
        when(tradeRepository.findByCodesAndAreaType(anyList(), anyString(), anyInt())).thenReturn(mockData);

        ChartDataResponse result = realEstateService.getPriceChartData(
            List.of("11680", "11650", "11710"), "UA04", 10
        );

        assertThat(result.getDatasets()).hasSize(3);
        assertThat(result.getDatasets().get(0).getData()).hasSize(2);
    }
}
