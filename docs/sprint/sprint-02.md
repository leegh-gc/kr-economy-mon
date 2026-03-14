# Sprint 2: ECOS API 연동 및 경제지표 핵심 4개 차트 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ECOS API 클라이언트와 JPA 엔티티를 구축하고, 금리/GDP/환율/물가 4개 차트를 실제 한국은행 데이터로 렌더링하여 경제 탭 상단 4개 섹션을 완성한다.

**Architecture:** `EcosApiService`(java.net.http.HttpClient 기반)가 한국은행 ECOS API를 호출하고, `EconomyIndicatorService`가 DB 캐시 유효성을 판단(stat_code + period 기준 최신 데이터 존재 여부)하여 stale이면 ECOS 재호출 후 upsert, 신선하면 DB에서 직접 반환한다. 각 REST 엔드포인트는 `ChartDataResponse` DTO로 응답하고 프론트엔드 common.js가 Chart.js로 렌더링한다.

**Tech Stack:** Spring Boot 3.3.2, Java 17+ (java.net.http.HttpClient), Spring Data JPA, QueryDSL 5.0.0 (Jakarta), PostgreSQL, Jackson ObjectMapper, Chart.js 4.4.3 (CDN), Bootstrap 5.3.3 (CDN)

**Sprint 기간:** Week 3~4 (2026-03-28 ~ 2026-04-10)

**Sprint 번호:** 2 (Phase 2 일부 대응: P2-1 ~ P2-6)

---

## 사전 준비 체크리스트

Sprint 착수 전 아래 항목을 확인한다:

- [ ] Sprint 1 완료 확인: `http://localhost:8086/krEconoMon` 접속 시 탭 2개 표시
- [ ] 환경변수 설정 확인: `echo $ECOS_API_KEY` (비어 있으면 안 됨)
- [ ] 환경변수 설정 확인: `echo $POSTGRES_USER`, `echo $POSTGRES_PASSWORD`
- [ ] PostgreSQL 접속 확인: `psql -h leeghhome.iptime.org -U $POSTGRES_USER -d aptprice_data -c "SELECT 1"`
- [ ] ECOS API 수동 확인: 브라우저에서 `http://ecos.bok.or.kr/api/StatisticSearch/{API_KEY}/json/kr/1/5/722Y001/M/202301/202301` 응답 확인

---

## Task 1: DB 연결 활성화 (application-local.yml AutoConfig exclude 제거)

**Files:**
- Modify: `src/main/resources/application-local.yml`

이 Task는 Sprint 1에서 로컬 개발 편의를 위해 DataSource AutoConfig를 비활성화했던 설정을 되돌린다. Phase 2부터 실제 DB가 필요하므로 exclude를 제거한다.

**Step 1: application-local.yml에서 autoconfigure.exclude 블록 제거**

`src/main/resources/application-local.yml` 파일에서 아래 블록을 삭제한다:

```yaml
# 삭제할 블록 (spring.autoconfigure.exclude 전체):
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

삭제 후 `application-local.yml`의 `spring:` 섹션은 `datasource:` 와 `jpa:` 만 남아야 한다:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:leeghhome.iptime.org}:${POSTGRES_PORT:5432}/${POSTGRES_DB:aptprice_data}
    username: ${POSTGRES_USER:}
    password: ${POSTGRES_PASSWORD:}
    driver-class-name: org.postgresql.Driver
    hikari:
      connection-timeout: 10000
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

app:
  ecos:
    api-key: ${ECOS_API_KEY:}
    base-url: http://ecos.bok.or.kr/api
  gemini:
    api-key: ${GEMINI_API_KEY:}
    model: gemini-2.0-flash
    timeout-seconds: 30

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Step 2: 애플리케이션 기동 확인**

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Expected: `HikariPool-1 - Start completed.` 로그 출력 후 서버 기동 성공 (8086 포트)

오류 발생 시 확인 사항:
- `POSTGRES_USER`, `POSTGRES_PASSWORD` 환경변수 미설정 → `.env` 파일 또는 IDE Run Configuration에서 설정
- `Connection refused` → PostgreSQL 서버 접근 가능 여부 확인

**Step 3: Commit**

```bash
git add src/main/resources/application-local.yml
git commit -m "chore: enable DataSource auto-configuration for local profile"
```

---

## Task 2: EcosApiResponse DTO 작성

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/dto/EcosApiResponse.java`

ECOS API의 JSON 응답 구조를 Jackson으로 파싱하기 위한 DTO. 실제 API 응답 예시:

```json
{
  "StatisticSearch": {
    "list_total_count": 120,
    "ROW": [
      { "STAT_CODE": "722Y001", "ITEM_CODE1": "0101000", "TIME": "202301", "DATA_VALUE": "3.5" }
    ]
  }
}
```

**Step 1: 실패 테스트 작성**

`src/test/java/com/example/kreconomonmon/dto/EcosApiResponseTest.java` 생성:

```java
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
                "ROW": [
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
```

**Step 2: 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.dto.EcosApiResponseTest" 2>&1 | tail -20
```

Expected: `FAILED` - `EcosApiResponse` 클래스 없음

**Step 3: EcosApiResponse DTO 구현**

`src/main/java/com/example/kreconomonmon/dto/EcosApiResponse.java` 생성:

```java
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
```

**Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.dto.EcosApiResponseTest"
```

Expected: `BUILD SUCCESSFUL` - 1 test passed

**Step 5: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/dto/EcosApiResponse.java \
        src/test/java/com/example/kreconomonmon/dto/EcosApiResponseTest.java
git commit -m "feat: add EcosApiResponse DTO with Jackson deserialization"
```

---

## Task 3: EcosApiService 구현 (ECOS API 클라이언트)

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/service/EcosApiService.java`
- Create: `src/test/java/com/example/kreconomonmon/service/EcosApiServiceTest.java`

`java.net.http.HttpClient` 기반으로 ECOS API를 호출하고, 최대 3회 재시도 로직을 포함한다.

조회 기간 자동 계산 규칙:
- 연(A): 현재연도-10 ~ 현재연도-1 (예: 2025년 기준 → 201501~202401)
- 분기(Q): 현재연도-10 1분기 ~ 현재연도-1 4분기 (예: 201501~202404)
- 월(M): 현재연도-10 1월 ~ 현재연도-1 12월 (예: 201501~202412)
- 일(D): 전년도 1/1 ~ 현재 날짜 (예: 20250101~20260314)

**Step 1: 실패 테스트 작성**

`src/test/java/com/example/kreconomonmon/service/EcosApiServiceTest.java` 생성:

```java
package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EcosApiServiceTest {

    @InjectMocks
    private EcosApiService ecosApiService;

    @Test
    void buildDateRange_annual() {
        // given
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        // when
        String[] range = ecosApiService.buildDateRange("A");

        // then: 연간은 10년 전부터 전년도까지
        int currentYear = LocalDate.now().getYear();
        assertThat(range[0]).isEqualTo(String.valueOf(currentYear - 10));
        assertThat(range[1]).isEqualTo(String.valueOf(currentYear - 1));
    }

    @Test
    void buildDateRange_monthly() {
        // given
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        // when
        String[] range = ecosApiService.buildDateRange("M");

        // then: 월간은 10년 전 01월부터 전년도 12월까지
        int currentYear = LocalDate.now().getYear();
        assertThat(range[0]).isEqualTo((currentYear - 10) + "01");
        assertThat(range[1]).isEqualTo((currentYear - 1) + "12");
    }

    @Test
    void buildDateRange_daily() {
        // given
        ReflectionTestUtils.setField(ecosApiService, "apiKey", "TEST_KEY");
        ReflectionTestUtils.setField(ecosApiService, "baseUrl", "http://ecos.bok.or.kr/api");

        // when
        String[] range = ecosApiService.buildDateRange("D");

        // then: 일간은 전년도 0101부터 오늘까지
        int currentYear = LocalDate.now().getYear();
        String today = LocalDate.now().toString().replace("-", "");
        assertThat(range[0]).isEqualTo((currentYear - 1) + "0101");
        assertThat(range[1]).isEqualTo(today);
    }
}
```

**Step 2: 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.service.EcosApiServiceTest" 2>&1 | tail -20
```

Expected: `FAILED` - `EcosApiService` 클래스 없음

**Step 3: EcosApiService 구현**

`src/main/java/com/example/kreconomonmon/service/EcosApiService.java` 생성:

```java
package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EcosApiService {

    @Value("${app.ecos.api-key}")
    private String apiKey;

    @Value("${app.ecos.base-url}")
    private String baseUrl;

    private static final int MAX_ROWS = 500;
    private static final int MAX_RETRY = 3;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ECOS API 호출. statCode, cycle, itemCode1을 받아 조회 기간을 자동 계산 후 데이터 반환.
     *
     * @param statCode  통계코드 (예: "722Y001")
     * @param cycle     주기 (A/Q/M/D)
     * @param itemCode1 항목코드 (예: "0101000")
     * @return 조회 결과 Row 리스트
     */
    public List<EcosApiResponse.Row> fetchStatistic(String statCode, String cycle, String itemCode1) {
        String[] range = buildDateRange(cycle);
        String startDate = range[0];
        String endDate = range[1];

        String url = String.format(
                "%s/StatisticSearch/%s/json/kr/1/%d/%s/%s/%s/%s/%s",
                baseUrl, apiKey, MAX_ROWS, statCode, cycle, startDate, endDate, itemCode1
        );

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.warn("ECOS API HTTP {}: attempt {}/{}", response.statusCode(), attempt, MAX_RETRY);
                    continue;
                }

                EcosApiResponse parsed = objectMapper.readValue(response.body(), EcosApiResponse.class);

                if (parsed.getStatisticSearch() == null || parsed.getStatisticSearch().getRows() == null) {
                    log.warn("ECOS API 응답에 데이터 없음 (statCode={}, attempt={})", statCode, attempt);
                    return List.of();
                }

                return parsed.getStatisticSearch().getRows();

            } catch (Exception e) {
                log.warn("ECOS API 호출 오류 (attempt {}/{}): {}", attempt, MAX_RETRY, e.getMessage());
                if (attempt == MAX_RETRY) {
                    log.error("ECOS API 최대 재시도 초과 (statCode={})", statCode, e);
                }
            }
        }

        return List.of();
    }

    /**
     * 주기(cycle)에 따라 조회 시작/종료 날짜 문자열 배열 반환. [startDate, endDate]
     */
    String[] buildDateRange(String cycle) {
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        return switch (cycle.toUpperCase()) {
            case "A" -> new String[]{
                    String.valueOf(currentYear - 10),
                    String.valueOf(currentYear - 1)
            };
            case "Q" -> new String[]{
                    (currentYear - 10) + "Q1",
                    (currentYear - 1) + "Q4"
            };
            case "M" -> new String[]{
                    (currentYear - 10) + "01",
                    (currentYear - 1) + "12"
            };
            case "D" -> new String[]{
                    (currentYear - 1) + "0101",
                    today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            };
            default -> throw new IllegalArgumentException("지원하지 않는 주기: " + cycle);
        };
    }
}
```

**Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.service.EcosApiServiceTest"
```

Expected: `BUILD SUCCESSFUL` - 3 tests passed

**Step 5: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/service/EcosApiService.java \
        src/test/java/com/example/kreconomonmon/service/EcosApiServiceTest.java
git commit -m "feat: implement EcosApiService with HttpClient and retry logic"
```

---

## Task 4: EconomyIndicator JPA Entity 및 Repository 구현

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/entity/EconomyIndicator.java`
- Create: `src/main/java/com/example/kreconomonmon/repository/EconomyIndicatorRepository.java`
- Create: `src/main/resources/db/migration/V1__create_economy_indicator.sql`

DB 스키마는 `ddl-auto: none`이므로 SQL로 직접 테이블을 생성한다. 운영 DB에 수동 적용하거나, 로컬 테스트 DB에서 실행한다.

**Step 1: DB 테이블 생성 SQL 작성**

`src/main/resources/db/migration/V1__create_economy_indicator.sql` 생성:

```sql
-- 경제지표 캐시 테이블
-- stat_code + item_code + period 조합이 유니크 키
CREATE TABLE IF NOT EXISTS economy_indicator (
    id          BIGSERIAL PRIMARY KEY,
    stat_code   VARCHAR(20)    NOT NULL,   -- ECOS 통계코드 (예: 722Y001)
    item_code   VARCHAR(30)    NOT NULL,   -- 항목코드 (예: 0101000)
    period      VARCHAR(10)    NOT NULL,   -- 기간 (예: 202301, 2023, 2023Q1)
    value       DECIMAL(20, 4),            -- 지표값
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_economy_indicator UNIQUE (stat_code, item_code, period)
);

CREATE INDEX IF NOT EXISTS idx_economy_indicator_stat_item
    ON economy_indicator (stat_code, item_code);
```

PostgreSQL에 접속하여 위 SQL을 실행한다:

```bash
psql -h leeghhome.iptime.org -U $POSTGRES_USER -d aptprice_data \
     -f src/main/resources/db/migration/V1__create_economy_indicator.sql
```

Expected: `CREATE TABLE` 출력

**Step 2: JPA Entity 실패 테스트 작성**

`src/test/java/com/example/kreconomonmon/entity/EconomyIndicatorTest.java` 생성:

```java
package com.example.kreconomonmon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class EconomyIndicatorTest {

    @Test
    void createEconomyIndicator() {
        EconomyIndicator indicator = EconomyIndicator.builder()
                .statCode("722Y001")
                .itemCode("0101000")
                .period("202301")
                .value(new BigDecimal("3.5"))
                .build();

        assertThat(indicator.getStatCode()).isEqualTo("722Y001");
        assertThat(indicator.getItemCode()).isEqualTo("0101000");
        assertThat(indicator.getPeriod()).isEqualTo("202301");
        assertThat(indicator.getValue()).isEqualByComparingTo("3.5");
    }
}
```

**Step 3: 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.entity.EconomyIndicatorTest" 2>&1 | tail -10
```

Expected: `FAILED` - `EconomyIndicator` 클래스 없음

**Step 4: EconomyIndicator Entity 구현**

`src/main/java/com/example/kreconomonmon/entity/EconomyIndicator.java` 생성:

```java
package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "economy_indicator",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_economy_indicator",
        columnNames = {"stat_code", "item_code", "period"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EconomyIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_code", nullable = false, length = 20)
    private String statCode;

    @Column(name = "item_code", nullable = false, length = 30)
    private String itemCode;

    @Column(name = "period", nullable = false, length = 10)
    private String period;

    @Column(name = "value", precision = 20, scale = 4)
    private BigDecimal value;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** upsert 시 값 갱신용 메서드 */
    public void updateValue(BigDecimal newValue) {
        this.value = newValue;
    }
}
```

**Step 5: EconomyIndicatorRepository 구현**

`src/main/java/com/example/kreconomonmon/repository/EconomyIndicatorRepository.java` 생성:

```java
package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.EconomyIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EconomyIndicatorRepository extends JpaRepository<EconomyIndicator, Long> {

    List<EconomyIndicator> findByStatCodeAndItemCodeOrderByPeriodAsc(String statCode, String itemCode);

    Optional<EconomyIndicator> findByStatCodeAndItemCodeAndPeriod(String statCode, String itemCode, String period);

    /** 특정 statCode + itemCode의 가장 최근 period 반환 */
    @Query("SELECT MAX(e.period) FROM EconomyIndicator e WHERE e.statCode = :statCode AND e.itemCode = :itemCode")
    Optional<String> findLatestPeriod(@Param("statCode") String statCode, @Param("itemCode") String itemCode);
}
```

**Step 6: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.entity.EconomyIndicatorTest"
```

Expected: `BUILD SUCCESSFUL` - 1 test passed

**Step 7: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/entity/EconomyIndicator.java \
        src/main/java/com/example/kreconomonmon/repository/EconomyIndicatorRepository.java \
        src/main/resources/db/migration/V1__create_economy_indicator.sql \
        src/test/java/com/example/kreconomonmon/entity/EconomyIndicatorTest.java
git commit -m "feat: add EconomyIndicator entity and repository"
```

---

## Task 5: ChartDataResponse DTO 작성

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/dto/ChartDataResponse.java`

REST API가 반환하는 공통 응답 형식. Chart.js의 `labels` / `datasets` 구조에 맞춘다.

**Step 1: ChartDataResponse DTO 작성**

`src/main/java/com/example/kreconomonmon/dto/ChartDataResponse.java` 생성:

```java
package com.example.kreconomonmon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Chart.js 렌더링용 공통 응답 DTO.
 * labels: X축 레이블 목록 (기간 문자열)
 * datasets: 시리즈별 데이터셋 목록
 */
@Getter
@Builder
public class ChartDataResponse {

    private List<String> labels;
    private List<Dataset> datasets;

    @Getter
    @Builder
    public static class Dataset {
        private String label;           // 범례 이름 (예: "기준금리")
        private List<Double> data;      // Y축 데이터 포인트
        private String borderColor;     // 선 색상 (예: "#007bff")
        private String backgroundColor; // 채우기 색상 (투명도 포함)
        private boolean fill;           // 영역 채우기 여부
        private String yAxisID;         // 이중 Y축 사용 시 축 ID (예: "y", "y1")
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/dto/ChartDataResponse.java
git commit -m "feat: add ChartDataResponse DTO for Chart.js rendering"
```

---

## Task 6: EconomyIndicatorService 구현 (DB 캐시 + ECOS API 연동)

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/service/EconomyIndicatorService.java`
- Create: `src/test/java/com/example/kreconomonmon/service/EconomyIndicatorServiceTest.java`

Freshness 판단 기준: 해당 statCode + itemCode의 최신 period가 기대 기간(주기별 현재 기준 최신)과 같으면 DB에서 반환, 아니면 ECOS 재호출.

**Step 1: 실패 테스트 작성**

`src/test/java/com/example/kreconomonmon/service/EconomyIndicatorServiceTest.java` 생성:

```java
package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EconomyIndicatorServiceTest {

    @Mock
    private EconomyIndicatorRepository repository;

    @Mock
    private EcosApiService ecosApiService;

    @InjectMocks
    private EconomyIndicatorService service;

    @Test
    void fetchFromDb_whenDataExists() {
        // given: DB에 데이터가 존재
        List<EconomyIndicator> dbData = List.of(
                EconomyIndicator.builder()
                        .statCode("722Y001").itemCode("0101000")
                        .period("202301").value(new BigDecimal("3.5")).build()
        );
        when(repository.findByStatCodeAndItemCodeOrderByPeriodAsc("722Y001", "0101000"))
                .thenReturn(dbData);

        // when
        List<EconomyIndicator> result = service.getIndicators("722Y001", "M", "0101000");

        // then: ECOS API를 호출하지 않고 DB 데이터 반환
        assertThat(result).hasSize(1);
        verify(ecosApiService, never()).fetchStatistic(any(), any(), any());
    }

    @Test
    void fetchFromEcos_whenDbEmpty() {
        // given: DB 비어 있음
        when(repository.findByStatCodeAndItemCodeOrderByPeriodAsc("722Y001", "0101000"))
                .thenReturn(List.of());

        EcosApiResponse.Row row = mock(EcosApiResponse.Row.class);
        when(row.getTime()).thenReturn("202301");
        when(row.getDataValue()).thenReturn("3.5");
        when(row.getStatCode()).thenReturn("722Y001");
        when(row.getItemCode1()).thenReturn("0101000");
        when(ecosApiService.fetchStatistic("722Y001", "M", "0101000"))
                .thenReturn(List.of(row));

        // when
        List<EconomyIndicator> result = service.getIndicators("722Y001", "M", "0101000");

        // then: ECOS API 호출 후 저장
        assertThat(result).hasSize(1);
        verify(ecosApiService).fetchStatistic("722Y001", "M", "0101000");
        verify(repository, atLeastOnce()).save(any(EconomyIndicator.class));
    }
}
```

**Step 2: 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.service.EconomyIndicatorServiceTest" 2>&1 | tail -10
```

Expected: `FAILED` - `EconomyIndicatorService` 클래스 없음

**Step 3: EconomyIndicatorService 구현**

`src/main/java/com/example/kreconomonmon/service/EconomyIndicatorService.java` 생성:

```java
package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomyIndicatorService {

    private final EconomyIndicatorRepository repository;
    private final EcosApiService ecosApiService;

    /**
     * DB에 데이터가 있으면 DB에서, 없으면 ECOS API에서 조회 후 저장하여 반환.
     */
    @Transactional
    public List<EconomyIndicator> getIndicators(String statCode, String cycle, String itemCode) {
        List<EconomyIndicator> dbData =
                repository.findByStatCodeAndItemCodeOrderByPeriodAsc(statCode, itemCode);

        if (!dbData.isEmpty()) {
            log.debug("DB 캐시 히트: statCode={}, itemCode={}, count={}", statCode, itemCode, dbData.size());
            return dbData;
        }

        log.info("DB 캐시 없음, ECOS API 호출: statCode={}, itemCode={}", statCode, itemCode);
        List<EcosApiResponse.Row> rows = ecosApiService.fetchStatistic(statCode, cycle, itemCode);

        List<EconomyIndicator> saved = rows.stream()
                .filter(row -> row.getDataValue() != null && !row.getDataValue().isBlank())
                .map(row -> {
                    BigDecimal value;
                    try {
                        value = new BigDecimal(row.getDataValue());
                    } catch (NumberFormatException e) {
                        log.warn("숫자 변환 실패: period={}, value={}", row.getTime(), row.getDataValue());
                        return null;
                    }
                    return EconomyIndicator.builder()
                            .statCode(statCode)
                            .itemCode(itemCode)
                            .period(row.getTime())
                            .value(value)
                            .build();
                })
                .filter(e -> e != null)
                .map(repository::save)
                .toList();

        return saved;
    }
}
```

**Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.service.EconomyIndicatorServiceTest"
```

Expected: `BUILD SUCCESSFUL` - 2 tests passed

**Step 5: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/service/EconomyIndicatorService.java \
        src/test/java/com/example/kreconomonmon/service/EconomyIndicatorServiceTest.java
git commit -m "feat: implement EconomyIndicatorService with DB cache and ECOS fallback"
```

---

## Task 7: common.js 차트 유틸 추가 및 금리 섹션 프론트엔드 구현

**Files:**
- Modify: `src/main/resources/static/js/common.js`
- Modify: `src/main/resources/templates/economy-tab.html` (섹션1 부분)

Chart.js 재사용 가능한 `createLineChart()` / `createBarChart()` 유틸 함수를 common.js에 추가하고, 금리 섹션 AJAX 호출 코드를 작성한다.

**Step 1: common.js에 차트 유틸 및 금리 섹션 로드 함수 추가**

`src/main/resources/static/js/common.js` 파일 끝에 아래 코드를 추가한다:

```javascript
// ============================================================
// Chart.js 유틸리티
// ============================================================

/**
 * 멀티 라인 차트 생성.
 * @param {string} canvasId - canvas 엘리먼트 ID
 * @param {Object} chartData - { labels: [], datasets: [] } (서버 응답과 동일 구조)
 * @param {Object} options - Chart.js options 오버라이드 (선택)
 */
function createLineChart(canvasId, chartData, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    // 기존 차트 인스턴스 파괴 (재렌더링 시 중복 방지)
    if (Chart.getChart(canvasId)) {
        Chart.getChart(canvasId).destroy();
    }

    const defaultOptions = {
        responsive: true,
        interaction: { mode: 'index', intersect: false },
        plugins: {
            legend: { position: 'top' },
            tooltip: { enabled: true }
        },
        scales: {
            x: { ticks: { maxTicksLimit: 12 } },
            y: { ticks: { callback: (v) => v.toLocaleString() } }
        }
    };

    new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: Object.assign({}, defaultOptions, options)
    });
}

/**
 * 막대 차트 생성 (양수=파랑, 음수=빨강 자동 적용).
 * @param {string} canvasId
 * @param {Object} chartData - { labels: [], datasets: [] }
 * @param {Object} options
 */
function createBarChart(canvasId, chartData, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    if (Chart.getChart(canvasId)) {
        Chart.getChart(canvasId).destroy();
    }

    // 양/음수에 따라 색상 배열 생성
    chartData.datasets.forEach(dataset => {
        dataset.backgroundColor = dataset.data.map(v =>
            v >= 0 ? 'rgba(0, 123, 255, 0.7)' : 'rgba(220, 53, 69, 0.7)'
        );
        dataset.borderColor = dataset.data.map(v =>
            v >= 0 ? 'rgba(0, 123, 255, 1)' : 'rgba(220, 53, 69, 1)'
        );
        dataset.borderWidth = 1;
    });

    const defaultOptions = {
        responsive: true,
        plugins: { legend: { position: 'top' } },
        scales: {
            y: { ticks: { callback: (v) => v.toFixed(1) + '%' } }
        }
    };

    new Chart(ctx, {
        type: 'bar',
        data: chartData,
        options: Object.assign({}, defaultOptions, options)
    });
}

// ============================================================
// 섹션별 데이터 로드 함수
// ============================================================

/** 섹션1: 금리 차트 로드 */
function loadInterestRateChart() {
    fetch('/api/economy/interest-rate')
        .then(res => {
            if (!res.ok) throw new Error('금리 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            createLineChart('interestRateChart', data);
        })
        .catch(err => {
            console.error(err);
            document.getElementById('interestRateChart').parentElement
                .insertAdjacentHTML('beforeend',
                    '<p class="text-danger">금리 데이터를 불러오지 못했습니다.</p>');
        });
}

/** 섹션2: GDP 차트 로드 */
function loadGdpChart() {
    fetch('/api/economy/gdp')
        .then(res => res.json())
        .then(data => createBarChart('gdpChart', data))
        .catch(err => console.error('GDP 데이터 로드 실패:', err));
}

/** 섹션3: 환율 차트 로드 (이중 Y축) */
function loadExchangeRateChart() {
    fetch('/api/economy/exchange-rate')
        .then(res => res.json())
        .then(data => {
            const options = {
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        title: { display: true, text: 'USD/EUR/CNY (원)' }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        title: { display: true, text: 'JPY 100엔 (원)' },
                        grid: { drawOnChartArea: false }
                    }
                }
            };
            createLineChart('exchangeRateChart', data, options);
        })
        .catch(err => console.error('환율 데이터 로드 실패:', err));
}

/** 섹션4: 물가지수 차트 로드 */
function loadPriceIndexChart() {
    fetch('/api/economy/price-index')
        .then(res => res.json())
        .then(data => createLineChart('priceIndexChart', data))
        .catch(err => console.error('물가 데이터 로드 실패:', err));
}

// 경제 탭 활성화 시 차트 로드
document.addEventListener('DOMContentLoaded', () => {
    const economyTab = document.getElementById('economy-tab');
    if (economyTab) {
        economyTab.addEventListener('shown.bs.tab', () => {
            loadInterestRateChart();
            loadGdpChart();
            loadExchangeRateChart();
            loadPriceIndexChart();
        });
        // 초기 로드 시 경제 탭이 활성화되어 있으면 즉시 로드
        if (economyTab.classList.contains('active')) {
            loadInterestRateChart();
            loadGdpChart();
            loadExchangeRateChart();
            loadPriceIndexChart();
        }
    }
});
```

**Step 2: economy-tab.html 섹션1~4에 canvas 엘리먼트 추가**

`src/main/resources/templates/economy-tab.html` 의 섹션1~4 placeholder를 canvas 태그로 교체한다. 섹션별 구조:

```html
<!-- 섹션1: 금리 -->
<div class="col-12 mb-4">
    <div class="card shadow-sm">
        <div class="card-header fw-bold">금리 동향 (기준금리 · 국고채 3년 · 국고채 10년)</div>
        <div class="card-body">
            <canvas id="interestRateChart" height="100"></canvas>
        </div>
    </div>
</div>

<!-- 섹션2: GDP -->
<div class="col-12 mb-4">
    <div class="card shadow-sm">
        <div class="card-header fw-bold">GDP 성장률 · 1인당 GDP</div>
        <div class="card-body">
            <canvas id="gdpChart" height="100"></canvas>
        </div>
    </div>
</div>

<!-- 섹션3: 환율 -->
<div class="col-12 mb-4">
    <div class="card shadow-sm">
        <div class="card-header fw-bold">환율 (USD · EUR · JPY 100엔 · CNY)</div>
        <div class="card-body">
            <canvas id="exchangeRateChart" height="100"></canvas>
        </div>
    </div>
</div>

<!-- 섹션4: 물가 -->
<div class="col-12 mb-4">
    <div class="card shadow-sm">
        <div class="card-header fw-bold">소비자물가지수(CPI) · 생산자물가지수(PPI)</div>
        <div class="card-body">
            <canvas id="priceIndexChart" height="100"></canvas>
        </div>
    </div>
</div>
```

**Step 3: Commit**

```bash
git add src/main/resources/static/js/common.js \
        src/main/resources/templates/economy-tab.html
git commit -m "feat: add Chart.js utilities and canvas elements for sections 1-4"
```

---

## Task 8: 섹션1 - 금리 REST API 실제 데이터 연동

**Files:**
- Modify: `src/main/java/com/example/kreconomonmon/controller/EconomyApiController.java`
- Create: `src/test/java/com/example/kreconomonmon/controller/InterestRateApiTest.java`

ECOS 통계 코드:
- 기준금리: statCode=`722Y001`, cycle=`M`, itemCode=`0101000`
- 국고채 3년: statCode=`817Y002`, cycle=`D`, itemCode=`010200000`
- 국고채 10년: statCode=`817Y002`, cycle=`D`, itemCode=`010210000`

**Step 1: 통합 테스트 작성 (Mock MVC)**

`src/test/java/com/example/kreconomonmon/controller/InterestRateApiTest.java` 생성:

```java
package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.entity.EconomyIndicator;
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

    @Test
    void getInterestRate_returnsChartData() throws Exception {
        // given
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

        // when & then
        mockMvc.perform(get("/api/economy/interest-rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.datasets").isArray())
                .andExpect(jsonPath("$.datasets.length()").value(3));
    }
}
```

**Step 2: 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.controller.InterestRateApiTest" 2>&1 | tail -20
```

Expected: `FAILED` - 컨트롤러가 mock 응답을 반환하므로 datasets 구조 불일치

**Step 3: EconomyApiController 금리 엔드포인트 구현**

`src/main/java/com/example/kreconomonmon/controller/EconomyApiController.java` 수정:

```java
package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/economy")
@RequiredArgsConstructor
public class EconomyApiController {

    private final EconomyIndicatorService economyIndicatorService;

    // ── 섹션1: 금리 ──────────────────────────────────────
    @GetMapping("/interest-rate")
    public ResponseEntity<ChartDataResponse> getInterestRate() {
        List<EconomyIndicator> baseRate =
                economyIndicatorService.getIndicators("722Y001", "M", "0101000");
        List<EconomyIndicator> bond3Y =
                economyIndicatorService.getIndicators("817Y002", "D", "010200000");
        List<EconomyIndicator> bond10Y =
                economyIndicatorService.getIndicators("817Y002", "D", "010210000");

        // 기준금리 기간 목록을 labels로 사용
        List<String> labels = baseRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(baseRate, "기준금리 (%)", "#dc3545", null),
                        toDataset(bond3Y,   "국고채 3년 (%)", "#007bff", null),
                        toDataset(bond10Y,  "국고채 10년 (%)", "#28a745", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션2: GDP ───────────────────────────────────────
    @GetMapping("/gdp")
    public ResponseEntity<ChartDataResponse> getGdp() {
        List<EconomyIndicator> growthRate =
                economyIndicatorService.getIndicators("902Y015", "Q", "KOR");
        List<EconomyIndicator> gdpPerCapita =
                economyIndicatorService.getIndicators("902Y018", "A", "KOR");

        List<String> labels = growthRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(growthRate,   "GDP 성장률 (%)", "#007bff", null),
                        toDataset(gdpPerCapita, "1인당 GDP (달러)", "#6c757d", "y1")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션3: 환율 ──────────────────────────────────────
    @GetMapping("/exchange-rate")
    public ResponseEntity<ChartDataResponse> getExchangeRate() {
        List<EconomyIndicator> usd =
                economyIndicatorService.getIndicators("731Y001", "D", "0000001");
        List<EconomyIndicator> eur =
                economyIndicatorService.getIndicators("731Y001", "D", "0000003");
        List<EconomyIndicator> jpy =
                economyIndicatorService.getIndicators("731Y001", "D", "0000002");
        List<EconomyIndicator> cny =
                economyIndicatorService.getIndicators("731Y001", "D", "0000053");

        List<String> labels = usd.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(usd, "USD (원)", "#007bff", "y"),
                        toDataset(eur, "EUR (원)", "#fd7e14", "y"),
                        toDataset(jpy, "JPY 100엔 (원)", "#dc3545", "y1"),  // 우축
                        toDataset(cny, "CNY (원)", "#28a745", "y")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션4: 물가 ──────────────────────────────────────
    @GetMapping("/price-index")
    public ResponseEntity<ChartDataResponse> getPriceIndex() {
        List<EconomyIndicator> cpi =
                economyIndicatorService.getIndicators("901Y009", "M", "0");
        List<EconomyIndicator> ppi =
                economyIndicatorService.getIndicators("404Y014", "M", "*AA");

        List<String> labels = cpi.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(cpi, "소비자물가지수 (CPI)", "#007bff", null),
                        toDataset(ppi, "생산자물가지수 (PPI)", "#fd7e14", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 나머지 엔드포인트 (이번 Sprint 미구현, mock 유지) ──
    @GetMapping("/trade")
    public ResponseEntity<Map<String, Object>> getTrade() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/employment")
    public ResponseEntity<Map<String, Object>> getEmployment() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/liquidity")
    public ResponseEntity<Map<String, Object>> getLiquidity() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/population")
    public ResponseEntity<Map<String, Object>> getPopulation() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────
    private ChartDataResponse.Dataset toDataset(
            List<EconomyIndicator> indicators,
            String label,
            String color,
            String yAxisID) {

        List<Double> data = indicators.stream()
                .map(e -> e.getValue() != null ? e.getValue().doubleValue() : null)
                .collect(Collectors.toList());

        ChartDataResponse.Dataset.DatasetBuilder builder = ChartDataResponse.Dataset.builder()
                .label(label)
                .data(data)
                .borderColor(color)
                .backgroundColor(color.replace(")", ", 0.1)").replace("rgb", "rgba"))
                .fill(false);

        if (yAxisID != null) {
            builder.yAxisID(yAxisID);
        }

        return builder.build();
    }
}
```

**Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.controller.InterestRateApiTest"
```

Expected: `BUILD SUCCESSFUL` - 1 test passed

**Step 5: 전체 테스트 통과 확인**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` - 모든 테스트 통과

**Step 6: Commit**

```bash
git add src/main/java/com/example/kreconomonmon/controller/EconomyApiController.java \
        src/test/java/com/example/kreconomonmon/controller/InterestRateApiTest.java
git commit -m "feat: implement interest-rate/gdp/exchange-rate/price-index REST endpoints"
```

---

## Task 9: 통합 동작 확인 (수동 E2E)

**Files:** 없음 (수동 검증)

**Step 1: 애플리케이션 기동**

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Expected: `Started KrEconoMonApplication in X.XXX seconds`

**Step 2: 금리 API 직접 호출**

```bash
curl -s http://localhost:8086/krEconoMon/api/economy/interest-rate | python3 -m json.tool | head -40
```

Expected:
```json
{
  "labels": ["202301", "202302", ...],
  "datasets": [
    { "label": "기준금리 (%)", "data": [3.5, 3.5, ...], ... },
    { "label": "국고채 3년 (%)", "data": [...], ... },
    { "label": "국고채 10년 (%)", "data": [...], ... }
  ]
}
```

**Step 3: 브라우저에서 차트 렌더링 확인**

`http://localhost:8086/krEconoMon` 접속 후:
- [ ] 경제 탭 클릭 시 금리 차트 렌더링 확인 (3개 라인)
- [ ] GDP 차트 렌더링 확인 (막대, 양수=파랑 확인)
- [ ] 환율 차트 렌더링 확인 (이중 Y축: JPY 우축 표시)
- [ ] 물가 차트 렌더링 확인 (CPI/PPI 2개 라인)

**Step 4: DB 데이터 저장 확인**

```bash
psql -h leeghhome.iptime.org -U $POSTGRES_USER -d aptprice_data \
     -c "SELECT stat_code, item_code, COUNT(*), MIN(period), MAX(period) FROM economy_indicator GROUP BY stat_code, item_code ORDER BY stat_code;"
```

Expected: 각 지표별 데이터 행 수 확인

---

## 완료 기준 (Definition of Done)

Sprint 2가 완료된 것으로 간주하는 기준:

- [ ] `./gradlew test` 전체 통과 (빌드 실패 없음)
- [ ] `economy_indicator` 테이블 생성 및 데이터 적재 확인
- [ ] `/api/economy/interest-rate` → `{ labels, datasets: [3개] }` 응답
- [ ] `/api/economy/gdp` → `{ labels, datasets: [2개] }` 응답
- [ ] `/api/economy/exchange-rate` → `{ labels, datasets: [4개] }` 응답 (JPY yAxisID: "y1")
- [ ] `/api/economy/price-index` → `{ labels, datasets: [2개] }` 응답
- [ ] 브라우저에서 경제 탭 클릭 시 4개 차트 실제 데이터로 렌더링
- [ ] 환율 차트 이중 Y축 정상 표시 (JPY 100엔 우측 Y축)
- [ ] 재기동 시 DB 캐시 히트 → ECOS API 미호출 (로그 확인)
- [ ] application-local.yml에 DataSource autoconfigure.exclude 블록 없음

---

## 예상 산출물

| 파일 유형 | 경로 | 설명 |
|-----------|------|------|
| Config | `src/main/resources/application-local.yml` | DataSource exclude 제거 |
| SQL | `src/main/resources/db/migration/V1__create_economy_indicator.sql` | economy_indicator 테이블 DDL |
| DTO | `src/main/java/.../dto/EcosApiResponse.java` | ECOS API JSON 파싱용 |
| DTO | `src/main/java/.../dto/ChartDataResponse.java` | Chart.js 응답 공통 DTO |
| Entity | `src/main/java/.../entity/EconomyIndicator.java` | JPA 엔티티 |
| Repository | `src/main/java/.../repository/EconomyIndicatorRepository.java` | Spring Data JPA |
| Service | `src/main/java/.../service/EcosApiService.java` | ECOS API 클라이언트 |
| Service | `src/main/java/.../service/EconomyIndicatorService.java` | DB 캐시 + ECOS 연동 |
| Controller | `src/main/java/.../controller/EconomyApiController.java` | 4개 엔드포인트 실구현 |
| JS | `src/main/resources/static/js/common.js` | Chart.js 유틸 + 섹션 로더 |
| Template | `src/main/resources/templates/economy-tab.html` | 섹션1~4 canvas 추가 |
| Test | `src/test/java/.../dto/EcosApiResponseTest.java` | DTO 역직렬화 테스트 |
| Test | `src/test/java/.../service/EcosApiServiceTest.java` | 날짜 범위 계산 테스트 |
| Test | `src/test/java/.../service/EconomyIndicatorServiceTest.java` | 캐시 로직 테스트 |
| Test | `src/test/java/.../controller/InterestRateApiTest.java` | API 응답 구조 테스트 |

---

## 다음 Sprint 예고 (Sprint 3)

Sprint 3에서는 Phase 2의 나머지 경제지표 섹션(무역/고용/유동성/주가지수)과 Phase 3의 서울 부동산 QueryDSL 연동을 다룬다:
- 섹션5~8: 무역, 고용, 가계부채/유동성, 주가지수 차트
- `stat_sigungu_yymm`, `stat_lease_sigungu` 테이블 QueryDSL 연동
- 부동산 탭 4개 지역 × 3개 구 차트
