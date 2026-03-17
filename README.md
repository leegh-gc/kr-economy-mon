# 🇰🇷 한국 경제 & 서울 부동산 현황 대시보드

> Spring Boot 3 + Chart.js + Gemini AI 기반 경제·부동산 지표 시각화 대시보드

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Chart.js](https://img.shields.io/badge/Chart.js-4.4.3-pink)](https://www.chartjs.org/)

---

## 📊 주요 기능

### 탭 1 — 한국 경제 현황
| 섹션 | 지표 | 출처 |
|------|------|------|
| 금리 | 기준금리, 국고채 3년·10년 | ECOS 722Y001, 817Y002 |
| GDP | 경제성장률(분기), 1인당 GNI(연간) | ECOS 902Y015, 902Y018 |
| 환율 | USD·EUR 월평균 환율 | ECOS 731Y004 |
| 물가 | 소비자물가지수, 생산자물가지수 | ECOS 901Y009, 404Y014 |
| 무역 | 경상수지, 수출, 수입 | ECOS 301Y013, 901Y118 |
| 고용 | 실업률(%), 취업자수(천명) — 이중 Y축 | ECOS 901Y027 |
| 통화·유동성 | M2 광의통화 | ECOS 902Y005 |
| 외환보유액 | 한국 외환보유액 | ECOS 902Y014 |
| 인구·출산율 | 총인구, 고령인구비율, 합계출산율 — 3축 | ECOS 901Y028 |
| AI 경제 분석 | Gemini 기반 일 1회 자동 분석 (수동 새로고침 가능) | Gemini API |

### 탭 2 — 서울 부동산 현황
| 섹션 | 내용 |
|------|------|
| KB 부동산 지수 | 서울 아파트 매매·전세 KB지수 추이 |
| 권역별 매매·전세 추이 | 강남권 / 강북권 / 강서권 / 중앙권 / **안양권** / **성남권** — 각 구별 평균가격 |
| TOP 10 순위 | 면적별(소형·중형·대형) 매매·전세 상위 10개 단지 |
| AI 부동산 분석 | Gemini 기반 일 1회 자동 분석 |

### 공통
- **방문자 수**: 오늘 방문자 / 누적 방문자 헤더 표시
- **최종 업데이트**: 경제 지표 최근 갱신 일시 자동 표시
- **AI 분석 타임스탬프**: 분석 수행 날짜·시간 표시

---

## 🗂 프로젝트 구조

```
kr-economy-mon/
├── src/main/java/com/example/kreconomonmon/
│   ├── controller/
│   │   ├── MainController.java              # 페이지 렌더링 + 방문자 기록
│   │   ├── EconomyApiController.java        # 경제 지표 REST API
│   │   ├── RealEstateApiController.java     # 부동산 REST API
│   │   ├── GeminiApiController.java         # AI 분석 REST API
│   │   └── VisitorApiController.java        # 방문자 집계 API
│   ├── service/
│   │   ├── EconomyIndicatorService.java     # DB-first 캐시 + ECOS 조회
│   │   ├── EcosApiService.java              # ECOS API 클라이언트 (재시도 3회)
│   │   ├── EconomyIndicatorScheduler.java   # 매일 01:00 지표 갱신 스케줄러
│   │   ├── GeminiAnalysisService.java       # AI 분석 (일별 캐시 + 동시성 락)
│   │   ├── GeminiApiService.java            # Gemini REST 클라이언트
│   │   ├── AnalysisCacheService.java        # SHA256 해시 기반 캐시 서비스
│   │   ├── RealEstateService.java           # 부동산 QueryDSL 조회
│   │   └── VisitorService.java              # 방문자 일별 집계
│   ├── repository/                          # Spring Data JPA + QueryDSL
│   ├── entity/                              # JPA 엔티티
│   └── dto/                                 # API 응답 DTO
├── src/main/resources/
│   ├── templates/
│   │   ├── index.html                       # Bootstrap 탭 전환
│   │   ├── layout/default.html              # 마스터 레이아웃
│   │   └── fragments/
│   │       ├── header.html                  # 방문자 수 + 업데이트 시각
│   │       ├── economy-tab.html             # 경제 탭 UI
│   │       ├── realestate-tab.html          # 부동산 탭 UI
│   │       └── region-content.html          # 권역 공통 컴포넌트
│   └── static/
│       ├── js/
│       │   ├── common.js                    # 공통 유틸 + 경제 차트
│       │   └── realestate.js                # 부동산 차트
│       └── css/custom.css
└── docs/ROADMAP.md
```

---

## 🗄 데이터베이스 테이블

| 테이블 | 용도 |
|--------|------|
| `economy_indicator` | ECOS API 수집 경제 지표 (stat_code, item_code, period, value) |
| `analysis_cache` | Gemini AI 분석 결과 캐시 (텍스트 + 이미지) |
| `visitor_count` | 일별 방문자 수 집계 |
| `stat_sigungu_yymm` | 서울 구별 아파트 매매가 (사전 적재) |
| `stat_lease_sigungu` | 서울 구별 아파트 전세가 (사전 적재) |
| `rank_uatype_sigungu` | 면적별 매매가 TOP 순위 |
| `rank_uatype_sigungu_lease` | 면적별 전세가 TOP 순위 |

### DDL 스크립트 (수동 생성 필요)

```sql
-- 방문자 수 집계 테이블
CREATE TABLE visitor_count (
    visit_date DATE PRIMARY KEY,
    count      BIGINT NOT NULL DEFAULT 0
);
```

> `economy_indicator`, `analysis_cache` 테이블은 Hibernate DDL auto로 자동 생성됩니다.
> 부동산 관련 테이블(`stat_sigungu_yymm` 등)은 별도 데이터 적재가 필요합니다.

---

## ⚙️ 환경 변수

```bash
ECOS_API_KEY        # 한국은행 ECOS Open API 키 (https://ecos.bok.or.kr)
GEMINI_API_KEY      # Google Gemini API 키
POSTGRES_HOST       # PostgreSQL 호스트
POSTGRES_PORT       # PostgreSQL 포트 (기본: 5432)
POSTGRES_DB         # 데이터베이스명
POSTGRES_USER       # DB 사용자
POSTGRES_PASSWORD   # DB 비밀번호
ADMIN_KEY           # AI 분석 캐시 강제 초기화용 관리자 키
```

---

## 🚀 실행 방법

### 개발 환경 (local 프로파일)

```bash
# 의존성 설치 및 빌드
./gradlew build

# 개발 서버 실행 (포트 8086)
./gradlew bootRun --args='--spring.profiles.active=local'
```

접속: `http://localhost:8086/krEconoMon`

### 운영 환경

```bash
./gradlew clean build
java -jar build/libs/kr-economy-mon.jar --spring.profiles.active=prod
```

### 테스트

```bash
./gradlew test                                          # 전체 테스트 (81개)
./gradlew test --tests "ClassName.methodName"           # 단일 테스트
```

---

## 🔌 주요 REST API

### 경제 지표

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/economy/interest-rate` | 기준금리·국고채 차트 데이터 |
| GET | `/api/economy/gdp` | GDP 성장률 |
| GET | `/api/economy/exchange-rate` | USD·EUR 월평균 환율 |
| GET | `/api/economy/price-index` | CPI·PPI |
| GET | `/api/economy/trade` | 경상수지·수출입 |
| GET | `/api/economy/employment` | 실업률·취업자수 (이중 Y축) |
| GET | `/api/economy/liquidity` | M2 통화량 |
| GET | `/api/economy/forex-reserve` | 외환보유액 |
| GET | `/api/economy/population` | 인구·출산율 |
| POST | `/api/economy/refresh` | 전체 지표 수동 갱신 |

### 부동산

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/real-estate/kb-index` | KB 매매·전세 지수 |
| GET | `/api/real-estate/price` | 권역별 매매가 추이 |
| GET | `/api/real-estate/lease` | 권역별 전세가 추이 |
| GET | `/api/real-estate/top5/trade` | 매매가 TOP 10 |
| GET | `/api/real-estate/top5/lease` | 전세가 TOP 10 |

### AI 분석

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/gemini/economy-analysis` | 경제 AI 분석 (일 1회 캐시) |
| GET | `/api/gemini/realestate-analysis` | 부동산 AI 분석 (일 1회 캐시) |
| POST | `/api/gemini/refresh` | 전체 캐시 초기화 (`X-Admin-Key` 헤더 필요) |
| POST | `/api/gemini/realestate-refresh` | 부동산 분석 캐시 초기화 |

### 기타

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/visitor` | 오늘·누적 방문자 수 |

---

## 🏗 아키텍처

```
Browser
  │ (Tab 클릭)
  ▼
Thymeleaf Page (MainController)
  │ (JS AJAX fetch)
  ▼
REST Controller (EconomyApiController / RealEstateApiController)
  │
  ├─ DB 캐시 확인 (economy_indicator / stat_sigungu_yymm)
  │     Hit → JSON 반환
  │     Miss ↓
  ├─ ECOS API 호출 (EcosApiService, 재시도 3회)
  │     → DB upsert (period 기준)
  │     → JSON 반환
  │
  └─ Chart.js 렌더링 (Browser)

AI 분석 흐름:
  Browser → GET /api/gemini/economy-analysis
    → AnalysisCacheService.findTodayCache() (당일 캐시 확인)
        Hit  → 캐시된 텍스트 + analysedAt 반환
        Miss → 동시성 락 획득
               → buildEconomyPrompt() (최신 지표 스냅샷 포함)
               → GeminiApiService.generateText()
               → saveCache() → 반환

스케줄러 (매일 01:00):
  EconomyIndicatorScheduler
    → INDICATORS (단일 itemCode, 20개)
    → PAIRED_INDICATORS (복수 itemCode, 731Y004·901Y027 등)
```

---

## 🗓 캐싱 전략

| 대상 | 전략 | 갱신 조건 |
|------|------|----------|
| 경제 지표 | DB 영속 캐시 (economy_indicator) | 매일 01:00 스케줄러 or `/api/economy/refresh` |
| Gemini 분석 텍스트 | 일별 캐시 (analysis_cache.updated_at) | 다음 날 첫 요청 or 수동 새로고침 버튼 |
| Gemini 만평 이미지 | 데이터 해시 캐시 (SHA256) | 지표 데이터 변경 시 자동 재생성 |

---

## 🛠 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Spring Boot 3.3.2, Java 21 |
| Template | Thymeleaf 3 + Layout Dialect 3.3.0 |
| DB | PostgreSQL 16 |
| ORM | Spring Data JPA + QueryDSL 5.0.0 (Jakarta) |
| Scheduler | Spring `@Scheduled` |
| HTTP Client | `java.net.http.HttpClient` (ECOS, Gemini) |
| Frontend | Bootstrap 5.3.3, Chart.js 4.4.3, Vanilla JS |
| AI | Google Gemini API (gemini-3-flash-preview, imagen-4.0-fast-generate-001) |
| Data API | 한국은행 ECOS Open API |
| Build | Gradle 8.8 |
| Test | JUnit 5, Mockito, Spring Boot Test (81 tests) |

---

## 📋 서울 권역 구성

| 권역 | 구 | sigungu_code |
|------|----|-------------|
| 강남권 | 강남구·강동구·서초구·송파구 | 11680·11740·11650·11710 |
| 강북권 | 강북구·성동구·노원구·성북구 | 11305·11200·11350·11290 |
| 강서권 | 강서구·영등포구·마포구·양천구 | 11500·11560·11440·11470 |
| 중앙권 | 종로구·중구·용산구·중랑구 | 11110·11140·11170·11260 |
| 안양권 | 만안구·동안구 | 41171·41173 |
| 성남권 | 수정구·중원구 | 41131·41133 |

---

## 🧪 테스트 현황

```
총 81개 테스트 — 전체 통과
├── controller/  16개
├── service/     50개
├── entity/       6개
├── repository/   2개
├── dto/          1개
└── config/       2개
```
