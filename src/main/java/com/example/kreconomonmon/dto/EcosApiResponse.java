package com.example.kreconomonmon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcosApiResponse {

    @JsonProperty("StatisticSearch")
    private StatisticSearch statisticSearch;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatisticSearch {

        @JsonProperty("list_total_count")
        private int listTotalCount;

        @JsonProperty("ROW")
        private List<Row> rows;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {

        @JsonProperty("STAT_CODE")
        private String statCode;

        @JsonProperty("STAT_NAME")
        private String statName;

        @JsonProperty("ITEM_CODE1")
        private String itemCode1;

        @JsonProperty("ITEM_NAME1")
        private String itemName1;

        @JsonProperty("TIME")
        private String time;

        @JsonProperty("DATA_VALUE")
        private String dataValue;
    }
}
