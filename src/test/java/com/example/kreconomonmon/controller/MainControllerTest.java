package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.VisitorService;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@Import(MainControllerTest.ThymeleafConfig.class)
class MainControllerTest {

    @TestConfiguration
    static class ThymeleafConfig {
        @Bean
        LayoutDialect layoutDialect() {
            return new LayoutDialect();
        }
    }

    @MockBean
    private VisitorService visitorService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexPageShouldReturn200() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"));
    }

    @Test
    void indexPageShouldContainTabElements() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("한국 경제 대시보드")))
               .andExpect(content().string(org.hamcrest.Matchers.containsString("서울 부동산 현황")));
    }
}
