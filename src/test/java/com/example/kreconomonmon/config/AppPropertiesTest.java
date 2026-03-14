package com.example.kreconomonmon.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
class AppPropertiesTest {

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Test
    void serverPortShouldBe8086() {
        assertThat(serverPort).isEqualTo(8086);
    }

    @Test
    void contextPathShouldBeKrEconoMon() {
        assertThat(contextPath).isEqualTo("/krEconoMon");
    }
}
