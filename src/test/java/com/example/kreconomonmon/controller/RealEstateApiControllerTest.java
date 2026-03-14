package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import com.example.kreconomonmon.service.RealEstateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RealEstateApiController.class)
class RealEstateApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RealEstateService realEstateService;

    @MockBean
    private EconomyIndicatorService economyIndicatorService;

    @Test
    void getKbIndex_returnsChartDataResponse() throws Exception {
        when(economyIndicatorService.getIndicators(any(), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/real-estate/kb-index"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.datasets").isArray());
    }

    @Test
    void getPrice_returnsChartDataResponse() throws Exception {
        when(realEstateService.getPriceChartData(any(), any()))
            .thenReturn(ChartDataResponse.builder()
                .labels(List.of("202301"))
                .datasets(List.of())
                .build());

        mockMvc.perform(get("/api/real-estate/price")
                .param("region", "gangnam")
                .param("areaType", "UA04")
                .param("codes", "11680,11650,11710"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray());
    }

    @Test
    void getLease_returnsChartDataResponse() throws Exception {
        when(realEstateService.getLeaseChartData(any(), any()))
            .thenReturn(ChartDataResponse.builder()
                .labels(List.of("202301"))
                .datasets(List.of())
                .build());

        mockMvc.perform(get("/api/real-estate/lease")
                .param("region", "gangnam")
                .param("areaType", "UA04")
                .param("codes", "11680,11650,11710"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray());
    }
}
