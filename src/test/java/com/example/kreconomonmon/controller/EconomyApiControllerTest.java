package com.example.kreconomonmon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EconomyApiController.class)
class EconomyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
