package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.GeminiAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeminiApiController.class)
class GeminiApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiAnalysisService geminiAnalysisService;

    @Test
    void getEconomyAnalysis_returns_analysis_map() throws Exception {
        when(geminiAnalysisService.getEconomyAnalysis())
            .thenReturn(Map.of(
                "status", "ok",
                "text", "현재 한국 경제는 금리 3.5%, 환율 1350원/달러로 안정적입니다.",
                "cached", false
            ));

        mockMvc.perform(get("/api/gemini/economy-analysis"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("ok"))
               .andExpect(jsonPath("$.text").isString());
    }

    @Test
    void getRealEstateAnalysis_returns_analysis_map() throws Exception {
        when(geminiAnalysisService.getRealEstateAnalysis())
            .thenReturn(Map.of(
                "status", "ok",
                "text", "서울 아파트 매매지수는 상승세를 유지하고 있습니다.",
                "cached", true
            ));

        mockMvc.perform(get("/api/gemini/realestate-analysis"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.text").isString());
    }
}
