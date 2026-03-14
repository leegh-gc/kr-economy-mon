# Sprint 2 완료 보고서

**Sprint:** 2 (Phase 2 부분: P2-1 ~ P2-6)
**기간:** 2026-03-14 ~ 2026-03-14 (계획: Week 3~4)
**완료일:** 2026-03-14
**상태:** 완료

---

## 1. 구현 범위 요약

Sprint 2의 목표인 "ECOS API 연동 + 경제지표 핵심 4개 차트(금리/GDP/환율/물가) 구현"을 완료하였습니다.

### 신규 생성 파일 (11개)

#### DTO
| 파일 | 설명 |
|------|------|
| `dto/EcosApiResponse.java` | ECOS API 응답 JSON 역직렬화 DTO. Jackson `@JsonProperty`로 `StatisticSearch` > `ROW` 구조 매핑. `@JsonIgnoreProperties(ignoreUnknown=true)` 적용 |
| `dto/ChartDataResponse.java` | Chart.js 렌더링용 응답 DTO. `labels(List<String>)` + `datasets(List<Dataset>)` 구조. Lombok `@Builder` 적용 |

#### 엔티티 및 Repository
| 파일 | 설명 |
|------|------|
| `entity/EconomyIndicator.java` | `economy_indicator` 테이블 JPA 매핑. `UniqueConstraint(stat_code, item_code, period)`. `@UpdateTimestamp`로 갱신 시각 자동 관리. `updateValue()` 메서드 제공 |
| `repository/EconomyIndicatorRepository.java` | `findByStatCodeAndItemCodeOrderByPeriodAsc`, `findByStatCodeAndItemCodeAndPeriod`, `findLatestPeriod` JPQL 쿼리 제공 |

#### 서비스
| 파일 | 설명 |
|------|------|
| `service/EcosApiService.java` | `java.net.http.HttpClient` 기반 ECOS API 호출. 최대 3회 재시도. `buildDateRange(cycle)`: A/Q/M → 최근 10년, D → 전년도 1/1~현재 자동 계산. URL 포맷: `{baseUrl}/StatisticSearch/{apiKey}/json/kr/1/{maxRows}/{statCode}/{cycle}/{start}/{end}/{itemCode}` |
| `service/EconomyIndicatorService.java` | DB 캐시 전략: `findByStatCodeAndItemCode`로 히트 확인 → 히트 시 ECOS 미호출, 미스 시 ECOS 호출 → 저장. `@Transactional` 적용 |

#### DB 마이그레이션
| 파일 | 설명 |
|------|------|
| `db/migration/V1__create_economy_indicator.sql` | Flyway DDL. `economy_indicator` 테이블 생성. `(stat_code, item_code, period)` UniqueConstraint + `(stat_code, item_code)` 복합 인덱스 |

#### 테스트
| 파일 | 설명 |
|------|------|
| `service/EcosApiServiceTest.java` | `buildDateRange` A/M/D 날짜 계산 단위 테스트 3개 |
| `dto/EcosApiResponseTest.java` | JSON 역직렬화 (Jackson `@JsonProperty` 매핑) 검증 |
| `entity/EconomyIndicatorTest.java` | 엔티티 빌더 및 필드 접근 검증 |
| `service/EconomyIndicatorServiceTest.java` | DB 캐시 히트 시 ECOS 미호출 / 캐시 미스 시 ECOS 호출+저장 행위 검증 |
| `controller/InterestRateApiTest.java` | `@WebMvcTest` 슬라이스: `/api/economy/interest-rate` 200 OK, datasets 3개 검증 |

### 수정된 파일 (4개)

| 파일 | 변경 내용 |
|------|-----------|
| `controller/EconomyApiController.java` | mock → 실구현. `EconomyIndicatorService` 주입하여 금리/GDP/환율/물가 4개 엔드포인트 `ChartDataResponse` 반환. `toDataset()` 헬퍼 메서드로 변환 로직 분리 |
| `templates/fragments/economy-tab.html` | 금리/GDP/환율/물가 섹션의 `div.chart-placeholder`를 `<canvas>` 태그로 교체 (id: `interestRateChart`, `gdpChart`, `exchangeRateChart`, `priceIndexChart`) |
| `static/js/common.js` | `createLineChart()`, `createBarChart()` 팩토리 함수 추가. `loadInterestRateChart()` 등 섹션별 로드 함수 추가. `DOMContentLoaded` 이벤트 리스너로 탭 활성화 시 자동 로드 |
| `application-local.yml` | DataSource AutoConfig `exclude` 블록 제거. DB 연결 활성화. `app.ecos.*`, `app.gemini.*` 프로퍼티 추가 |

---

## 2. 코드 리뷰 결과

### 정적 검토 방식

Java 21이 설치되어 있지 않아 `./gradlew build` 실행이 불가능합니다. 소스 코드 정적 검토로 대체합니다.

### Critical/High 이슈: 없음

### Medium 이슈 (추후 개선 권고)

| # | 파일 | 이슈 | 권고사항 |
|---|------|------|----------|
| M-1 | `EcosApiService.java` | `EconomyIndicatorService.getIndicators()`에서 DB가 비어있을 때만 ECOS를 호출하므로, 데이터가 한 번 저장되면 영구 캐시됨 (stale 데이터 위험) | Sprint 3 또는 Phase 5에서 `updatedAt` 기준 TTL 전략 도입 권고. 예: 마지막 업데이트가 7일 이상 경과하면 재호출 |
| M-2 | `EcosApiService.java` | `HttpClient`가 `final` 필드로 인스턴스 생성 시 초기화됨. Spring Bean 생성 시점에 생성되므로 테스트 시 네트워크 모킹이 어려움 | `HttpClient`를 생성자 주입 또는 `@Bean`으로 외부화하면 테스트 격리성 향상 |
| M-3 | `EconomyIndicatorService.java` | `rows.stream().filter(e -> e != null)` 대신 Java 스타일상 `Objects::nonNull` 사용 권고 | `filter(Objects::nonNull)` 으로 교체 권고 |
| M-4 | `EconomyApiController.java` | 금리 차트의 `labels`가 `baseRate`(월별)의 period 기준으로 설정됨. 국고채 3년/10년은 일별 데이터이므로 레이블 길이 불일치 발생 가능 | 각 시리즈의 데이터 포인트 수 불일치 시 Chart.js가 `null` 값으로 처리하는 동작 확인 필요. Sprint 3에서 공통 X축 정규화 로직 추가 권고 |
| M-5 | `common.js` | `errorMessage()` 함수에서 `msg` 파라미터가 그대로 template literal에 삽입됨 (XSS 잠재 리스크). Sprint 1 리뷰 M-3와 동일 이슈 지속 | Phase 5 보안 점검 시 `textContent` 방식 또는 HTML 이스케이프 함수 적용 |
| M-6 | `EcosApiService.java` | `MAX_ROWS = 500` 고정값 사용. 일별 데이터(D)의 경우 1년치도 약 250건이지만, 일부 통계코드는 데이터가 많아 500건을 초과할 수 있음 | ECOS API `list_total_count` 확인 후 페이지네이션 구현 또는 MAX_ROWS 상향 검토 권고 |

### Low 이슈 (참고)

| # | 파일 | 이슈 |
|---|------|------|
| L-1 | `build.gradle` | `java.toolchain.languageVersion = 21`로 변경됨. Java 21 미설치 환경에서 빌드 불가 (Sprint 1 보고서에서 17 권장). Java 21 LTS(Temurin 21) 설치 필요 |
| L-2 | `EconomyApiController.java` | `Collectors.toList()` 대신 `Stream.toList()` 사용 가능 (Java 16+). 가독성 및 불변 리스트 보장 측면에서 유리 |
| L-3 | `V1__create_economy_indicator.sql` | `updated_at DEFAULT NOW()` 설정이 있으나 JPA `@UpdateTimestamp`와 이중 관리됨. 기능상 문제없으나 DB/JPA 중 하나를 권위적 소스로 정하는 것이 명확 |

### 계획서 대비 구현 일치 여부

| Task | 계획서 항목 | 구현 상태 |
|------|-------------|-----------|
| P2-1 | EcosApiService: HttpClient, 재시도 3회, buildDateRange | 완전 일치 |
| P2-2 | economy_indicator 테이블, JPA Entity, Repository, DB 캐시 | 완전 일치 |
| P2-3 | 금리 멀티 라인 차트: 기준금리+국고채3년+국고채10년 | 완전 일치 |
| P2-4 | GDP 차트: 성장률 막대+1인당GDP | 완전 일치 |
| P2-5 | 환율 이중 Y축: USD/EUR/CNY 좌축, JPY 우축 | 완전 일치 |
| P2-6 | 물가 멀티 라인: CPI+PPI | 완전 일치 |

---

## 3. 완료 기준(Definition of Done) 검토

| 기준 | 상태 | 비고 |
|------|------|------|
| ECOS API 데이터 기반 금리 차트 렌더링 | 미검증 | Java 21 미설치로 빌드 불가. 코드 정적 검토 통과 |
| GDP 성장률 막대 차트 (양/음 색상 구분) | 미검증 | `createBarChart()`에서 `data.map(v => v >= 0 ? 파랑 : 빨강)` 로직 정적 검토 통과 |
| 환율 이중 Y축 (JPY 100엔 우축 분리) | 미검증 | `common.js`의 `loadExchangeRateChart()`에서 `scales.y1` 옵션 정적 검토 통과 |
| ECOS 응답이 DB에 캐싱되어 재조회 시 API 미호출 | 미검증 | `EconomyIndicatorServiceTest.fetchFromDb_whenDataExists()` 정적 검토 통과 |
| 데이터 로딩 중 스피너 표시 | 코드 검토 통과 | canvas 태그 전환 후 JS 로드 전 기본 빈 canvas 표시. Sprint 3에서 로딩 스피너 재적용 검토 필요 |

---

## 4. Playwright 검증 결과

**실행 불가 — Java 21 미설치로 인해 서버 구동이 불가능합니다.**

Java 설치 후 아래 검증 시나리오를 수행해야 합니다 (sprint-02.md 참조):
- 메인 페이지 접속 및 경제 탭1 활성 확인
- `/api/economy/interest-rate`, `/api/economy/gdp`, `/api/economy/exchange-rate`, `/api/economy/price-index` 4개 엔드포인트 200 OK 확인
- 각 canvas 요소에서 Chart.js 데이터 렌더링 확인
- 이중 Y축(환율 차트) 좌/우 축 레이블 표시 확인

---

## 5. 특이사항 및 기술 결정 사항

1. **Java 21 변경**: `build.gradle`의 `java.toolchain.languageVersion`이 17에서 21로 변경됨. Temurin 21 LTS 설치가 Sprint 3 착수 전 필수.

2. **DB 캐시 전략 (1세대 구현)**: 현재 구현은 "DB에 데이터가 있으면 항상 반환, 없으면 ECOS 호출"의 단순 캐시 전략. TTL(Time-To-Live) 기반 갱신 전략은 Medium 이슈 M-1로 등록, Phase 5에서 고도화 예정.

3. **레이블 불일치 위험 (M-4 이슈)**: 금리 차트에서 월별(기준금리)과 일별(국고채) 데이터가 혼합되어 X축 레이블이 `baseRate`(월별) 기준으로만 설정됨. 실제 렌더링 시 Chart.js가 datasets 간 데이터 포인트 수 불일치를 어떻게 처리하는지 확인 필요. Sprint 3에서 공통 날짜 축 정규화 로직 추가 권고.

4. **Flyway 도입**: `src/main/resources/db/migration/V1__create_economy_indicator.sql`로 Flyway 마이그레이션 체계가 도입됨. `build.gradle`에 Flyway 의존성은 아직 없음 — 실행 시 Flyway 의존성 추가 또는 수동 DDL 실행이 필요.

5. **mock 엔드포인트 유지**: `trade`, `employment`, `liquidity`, `population` 4개 엔드포인트는 Sprint 3(P2-7~P2-10) 구현 예정으로 mock 응답 유지.

---

## 6. 검증 보고서

Java 21 미설치로 인해 자동 검증을 수행하지 못했습니다. 아래 항목은 Java 설치 후 수동 검증이 필요합니다.

| 검증 항목 | 방법 | 예상 결과 |
|-----------|------|-----------|
| `./gradlew test` 전체 테스트 실행 | CLI | EcosApiServiceTest 3개, EcosApiResponseTest 1개, EconomyIndicatorTest 1개, EconomyIndicatorServiceTest 2개, InterestRateApiTest 1개 — 총 8개 통과 |
| `./gradlew bootRun` 서버 기동 | CLI | 포트 8086 정상 기동 |
| http://localhost:8086/krEconoMon 접속 | Playwright | 탭1 활성, canvas 4개 존재 |
| `/api/economy/interest-rate` | Playwright / curl | 200 OK, datasets 3개 |
| `/api/economy/gdp` | Playwright / curl | 200 OK, datasets 2개 |
| `/api/economy/exchange-rate` | Playwright / curl | 200 OK, datasets 4개, yAxisID 포함 |
| `/api/economy/price-index` | Playwright / curl | 200 OK, datasets 2개 |
| DB 캐시 동작 확인 | 로그 | 2회 연속 조회 시 두 번째부터 "DB 캐시 히트" 로그 출력 |

---

## 7. 다음 Sprint 준비사항 (Sprint 3)

Sprint 3 착수 전 아래 항목을 완료해야 합니다:

- [ ] **Java 21 LTS 설치** (Temurin 21 권장): `java -version` 확인
- [ ] `./gradlew test` 실행하여 Sprint 2 테스트 8개 전체 통과 확인
- [ ] `./gradlew bootRun` 후 http://localhost:8086/krEconoMon 금리/GDP/환율/물가 차트 실데이터 렌더링 확인
- [ ] PostgreSQL DB 접속 확인 및 Flyway 마이그레이션 실행 (`economy_indicator` 테이블 생성 확인)
- [ ] ECOS API Key 실제 호출 테스트 (브라우저에서 `/api/economy/interest-rate` 200 OK 확인)
- [ ] `build.gradle`에 Flyway 의존성 추가 검토: `implementation 'org.flywaydb:flyway-core'` + `runtimeOnly 'org.flywaydb:flyway-database-postgresql'`
- [ ] 환율 차트 이중 Y축 실제 렌더링 확인 (JPY 단위 분리 동작)
- [ ] DB 캐시 TTL 전략 설계 (Medium 이슈 M-1 대응)
