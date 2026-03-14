package com.example.kreconomonmon.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EcosApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializeEcosResponse() throws Exception {
        String json = """
            {
              "StatisticSearch": {
                "list_total_count": 1,
                "row": [
                  {
                    "STAT_CODE": "722Y001",
                    "STAT_NAME": "기준금리",
                    "ITEM_CODE1": "0101000",
                    "ITEM_NAME1": "한국은행 기준금리",
                    "TIME": "202301",
                    "DATA_VALUE": "3.5"
                  }
                ]
              }
            }
            """;

        EcosApiResponse response = objectMapper.readValue(json, EcosApiResponse.class);

        assertThat(response.getStatisticSearch()).isNotNull();
        assertThat(response.getStatisticSearch().getRows()).hasSize(1);
        assertThat(response.getStatisticSearch().getRows().get(0).getDataValue()).isEqualTo("3.5");
        assertThat(response.getStatisticSearch().getRows().get(0).getTime()).isEqualTo("202301");
    }
}
