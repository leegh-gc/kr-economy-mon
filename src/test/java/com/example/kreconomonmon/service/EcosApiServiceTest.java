package com.example.kreconomonmon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EcosApiServiceTest {

    @InjectMocks
    private EcosApiService ecosApiService;

    @Test
    void buildDateRange_annual() {
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        String[] range = ecosApiService.buildDateRange("A");

        int currentYear = LocalDate.now().getYear();
        assertThat(range[0]).isEqualTo(String.valueOf(currentYear - 30));
        assertThat(range[1]).isEqualTo(String.valueOf(currentYear - 1));
    }

    @Test
    void buildDateRange_monthly() {
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        String[] range = ecosApiService.buildDateRange("M");

        int currentYear = LocalDate.now().getYear();
        String expectedEnd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        assertThat(range[0]).isEqualTo((currentYear - 10) + "01");
        assertThat(range[1]).isEqualTo(expectedEnd);
    }

    @Test
    void buildDateRange_daily() {
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        String[] range = ecosApiService.buildDateRange("D");

        int currentYear = LocalDate.now().getYear();
        String today = LocalDate.now().toString().replace("-", "");
        assertThat(range[0]).isEqualTo((currentYear - 1) + "0101");
        assertThat(range[1]).isEqualTo(today);
    }
}
