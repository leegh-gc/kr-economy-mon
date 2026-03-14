package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.EconomyIndicatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EconomyApiController.class)
class EconomyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EconomyIndicatorService economyIndicatorService;

    @BeforeEach
    void setUp() {
        when(economyIndicatorService.getIndicators(any(), any(), any())).thenReturn(List.of());
    }

    @Test
    void interestRateEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/economy/interest-rate"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void gdpEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/economy/gdp"))
               .andExpect(status().isOk());
    }

    @Test
    void getTrade_returnsChartDataResponse() throws Exception {
        mockMvc.perform(get("/api/economy/trade"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.datasets").isArray());
    }

    @Test
    void getEmployment_returnsChartDataResponse() throws Exception {
        mockMvc.perform(get("/api/economy/employment"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.datasets").isArray());
    }

    @Test
    void getLiquidity_returnsChartDataResponse() throws Exception {
        mockMvc.perform(get("/api/economy/liquidity"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.datasets").isArray());
    }

    @Test
    void getPopulation_returnsChartDataResponse() throws Exception {
        mockMvc.perform(get("/api/economy/population"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.datasets").isArray());
    }
}
