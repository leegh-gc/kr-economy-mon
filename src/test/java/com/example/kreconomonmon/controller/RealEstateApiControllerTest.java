package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.dto.Top5Response;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import com.example.kreconomonmon.service.RealEstateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void getTop5Trade_returns_list_response() throws Exception {
        when(realEstateService.getTop5Trade(eq(List.of("11680")), eq("UA04")))
            .thenReturn(List.of(
                Top5Response.builder()
                    .aptName("래미안대치팰리스")
                    .dongName("대치동")
                    .buildYear(2002)
                    .avgPrice(new BigDecimal("250000"))
                    .minPrice(new BigDecimal("220000"))
                    .maxPrice(new BigDecimal("280000"))
                    .dealCount(5)
                    .build()
            ));

        mockMvc.perform(get("/api/real-estate/top5/trade")
                .param("codes", "11680")
                .param("areaType", "UA04"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].aptName").value("래미안대치팰리스"))
               .andExpect(jsonPath("$[0].dealCount").value(5));
    }

    @Test
    void getTop5Lease_returns_list_response() throws Exception {
        when(realEstateService.getTop5Lease(eq(List.of("11680")), eq("UA04")))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/real-estate/top5/lease")
                .param("codes", "11680")
                .param("areaType", "UA04"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray());
    }
}
