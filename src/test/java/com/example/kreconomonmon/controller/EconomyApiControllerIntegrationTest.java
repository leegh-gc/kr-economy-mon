package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.service.EconomyIndicatorScheduler;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EconomyApiController.class)
class EconomyApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EconomyIndicatorService economyIndicatorService;

    @MockBean
    private EconomyIndicatorScheduler economyIndicatorScheduler;

    private EconomyIndicator indicator(String statCode, String itemCode, String period, double value) {
        return EconomyIndicator.builder()
            .statCode(statCode).itemCode(itemCode).period(period)
            .value(new BigDecimal(value)).updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void getInterestRate_returns_chart_data_with_labels() throws Exception {
        when(economyIndicatorService.getIndicators(eq("722Y001"), any(), any()))
            .thenReturn(List.of(indicator("722Y001", "0101000", "202502", 3.5)));
        when(economyIndicatorService.getIndicators(eq("817Y002"), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/interest-rate"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.labels").isArray())
               .andExpect(jsonPath("$.labels[0]").value("202502"))
               .andExpect(jsonPath("$.datasets").isArray())
               .andExpect(jsonPath("$.datasets[0].label").value("기준금리 (%)"));
    }

    @Test
    void getGdp_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("902Y015"), any(), any()))
            .thenReturn(List.of(indicator("902Y015", "KOR", "2024Q4", 2.1)));
        when(economyIndicatorService.getIndicators(eq("902Y018"), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/gdp"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("GDP 성장률 (%)"));
    }

    @Test
    void getExchangeRate_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("731Y001"), any(), eq("0000001")))
            .thenReturn(List.of(indicator("731Y001", "0000001", "20250301", 1380.0)));
        when(economyIndicatorService.getIndicators(eq("731Y001"), any(), argThat(c -> !c.equals("0000001"))))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/exchange-rate"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("USD (원)"));
    }

    @Test
    void getPriceIndex_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("901Y009"), any(), any()))
            .thenReturn(List.of(indicator("901Y009", "0", "202502", 113.5)));
        when(economyIndicatorService.getIndicators(eq("404Y014"), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/price-index"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("소비자물가지수 (CPI)"));
    }

    @Test
    void getTrade_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("301Y013"), any(), any()))
            .thenReturn(List.of(indicator("301Y013", "000000", "202502", 5000.0)));
        when(economyIndicatorService.getIndicators(eq("901Y118"), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/trade"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("경상수지 (백만달러)"));
    }

    @Test
    void getEmployment_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("901Y027"), any(), eq("I61BC")))
            .thenReturn(List.of(indicator("901Y027", "I61BC", "202502", 3.1)));
        when(economyIndicatorService.getIndicators(eq("901Y027"), any(), eq("I61BA")))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/employment"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("실업률 (%)"));
    }

    @Test
    void getLiquidity_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("902Y005"), any(), any()))
            .thenReturn(List.of(indicator("902Y005", "KR", "202502", 3800.0)));
        when(economyIndicatorService.getIndicators(eq("902Y014"), any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/liquidity"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets[0].label").value("M2 광의통화 (조원)"));
    }

    @Test
    void getPopulation_returns_chart_data() throws Exception {
        when(economyIndicatorService.getIndicators(eq("901Y028"), any(), eq("I35A")))
            .thenReturn(List.of(indicator("901Y028", "I35A", "2023", 51700.0)));
        when(economyIndicatorService.getIndicators(eq("901Y028"), any(), argThat(c -> !c.equals("I35A"))))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/population"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.datasets").isArray());
    }
}
