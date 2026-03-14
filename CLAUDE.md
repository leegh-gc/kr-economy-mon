# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**한국 경제 & 서울 부동산 현황 대시보드** — A single-page Spring Boot web app with two tabs:
- **Tab 1**: Korean economic indicators (GDP, interest rates, exchange rates, CPI, trade, unemployment, household debt, stock index)
- **Tab 2**: Seoul real estate data (KB price index, 4 regional zones × 3 districts, TOP5 price/lease rankings)

Server runs at `http://localhost:8086/krEconoMon`

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3.2 MVC |
| Templating | Thymeleaf + Layout Dialect 3.3.0 |
| DB Access | Spring Data JPA + QueryDSL 5.0.0 (Jakarta) |
| Database | PostgreSQL (`leeghhome.iptime.org:5432/aptprice_data`) |
| Charts | Chart.js 4.4.3 (CDN, loaded via AJAX) |
| CSS | Bootstrap 5.3.3 (CDN) |
| AI | Gemini REST API via `java.net.http.HttpClient` |
| Economic Data | ECOS API (한국은행) |
| Build | Gradle 8.8, Java 17+ |

## Build & Run Commands

```bash
./gradlew build           # Full build with tests
./gradlew bootRun         # Run with hot reload (dev)
./gradlew clean build     # Clean rebuild
./gradlew test            # Run all tests
./gradlew test --tests "ClassName.methodName"  # Single test
java -jar build/libs/kr-economy-mon.jar --spring.profiles.active=prod  # Production
```

## Required Environment Variables

```
ECOS_API_KEY        # 한국은행 ECOS API key
GEMINI_API_KEY      # Google Gemini API key
POSTGRES_HOST       # leeghhome.iptime.org
POSTGRES_PORT       # 5432
POSTGRES_DB         # aptprice_data
POSTGRES_USER
POSTGRES_PASSWORD
```

## Architecture

### Request Flow
```
Browser (Tab click)
  → Thymeleaf page render (MainController)
  → JavaScript AJAX call → REST endpoint (EconomyApiController / RealEstateApiController)
  → Service layer (data check + ECOS API call if stale)
  → JPA/QueryDSL repository → PostgreSQL
  → JSON response → Chart.js render
  → On data change → GeminiApiController → AnalysisCacheService
```

### Package Structure
```
com.example.kreconomonmon/
├── controller/    # MainController, EconomyApiController, RealEstateApiController, GeminiApiController
├── service/       # EcosApiService, GeminiApiService, EconomyIndicatorService, RealEstateService, AnalysisCacheService
├── repository/    # Spring Data JPA + QueryDSL repositories
├── entity/        # JPA entities (EconomyIndicator, AnalysisCache, StatSigunguYymm, StatLeaseSigungu, RankUatypeSigungu*)
└── dto/           # EcosApiResponse, GeminiApiResponse, ChartDataResponse, ApiErrorResponse
```

### Spring Profiles
- `application-local.yml` — development
- `application-prod.yml` — production

### Caching Strategy
Server-side cache in `analysis_cache` PostgreSQL table. On page load:
1. Compare current data hash (SHA256) vs cached hash
2. If unchanged → return cached Gemini analysis text + cartoon
3. If changed → re-call Gemini API → update cache
Gemini must not be called redundantly.

### QueryDSL Pattern for Real Estate
Dynamic filters use `BooleanBuilder` with sigungu codes, use-area types, and year ranges. The 4 Seoul regions map to specific district codes.

### ECOS API
- Endpoint: `http://ecos.bok.or.kr/api/StatisticSearch`
- HTTP client: `java.net.http.HttpClient`
- Retry: max 3 attempts

### Gemini API
- Model: `gemini-3-flash` (for cartoon generation)
- Timeout: 30 seconds
- Generates: analysis text + 컷툰(cartoon) image per tab

## Key DB Tables (Pre-loaded Real Estate Data)
- `stat_sigungu_yymm` — apartment purchase price by district/month
- `stat_lease_sigungu` — lease(전세) price by district
- `rank_uatype_sigungu` — TOP5 purchase price rankings by area type
- `rank_uatype_sigungu_lease` — TOP5 lease price rankings by area type

## Development Phases (see docs/ROADMAP.md)
The project is **greenfield** — no source code exists yet. Implementation follows 5 phases over 10 weeks per `docs/ROADMAP.md`. Each phase has Playwright MCP acceptance test scenarios defined.
