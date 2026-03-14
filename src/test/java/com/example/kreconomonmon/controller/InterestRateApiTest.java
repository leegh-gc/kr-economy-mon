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
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EconomyApiController.class)
class InterestRateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EconomyIndicatorService economyIndicatorService;

    @MockBean
    private EconomyIndicatorScheduler economyIndicatorScheduler;

    @Test
    void getInterestRate_returnsChartData() throws Exception {
        List<EconomyIndicator> baseRateData = List.of(
                EconomyIndicator.builder()
                        .statCode("722Y001").itemCode("0101000")
                        .period("202301").value(new BigDecimal("3.5")).build(),
                EconomyIndicator.builder()
                        .statCode("722Y001").itemCode("0101000")
                        .period("202302").value(new BigDecimal("3.5")).build()
        );

        when(economyIndicatorService.getIndicators(eq("722Y001"), eq("M"), eq("0101000")))
                .thenReturn(baseRateData);
        when(economyIndicatorService.getIndicators(eq("817Y002"), eq("D"), eq("010200000")))
                .thenReturn(List.of());
        when(economyIndicatorService.getIndicators(eq("817Y002"), eq("D"), eq("010210000")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/economy/interest-rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.datasets").isArray())
                .andExpect(jsonPath("$.datasets.length()").value(3));
    }
}
