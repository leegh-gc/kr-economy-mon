# 프로젝트 로드맵

## 개요

| 항목 | 내용 |
|------|------|
| 프로젝트 | 한국 경제 & 서울 부동산 현황 대시보드 |
| 목표 | ECOS API 경제지표 8개 섹션 + 서울 부동산 4개 권역 데이터를 단일 페이지 2탭 대시보드로 제공, Gemini AI 분석 및 컷툰 생성 포함 |
| 전체 예상 기간 | 약 10주 (5 Sprint, Sprint당 2주) |
| 현재 진행 단계 | Phase 4 진행 중 (Sprint 4 완료, Sprint 5 예정) |
| 팀 규모 | 2-3명 (백엔드 1-2명, 프론트엔드 1명) |

## 기술 스택

| 영역 | 기술 | 선택 이유 |
|------|------|-----------|
| 백엔드/프론트 | Spring Boot 3.3.2 MVC + Thymeleaf Layout Dialect 3.3.0 | PRD 명세, 서버 사이드 렌더링으로 SEO 및 초기 로딩 유리 |
| DB 접근 | Spring Data JPA + QueryDSL 5.0.0 (Jakarta) | 타입 안전 동적 쿼리 지원 |
| DB | PostgreSQL (leeghhome.iptime.org:5432/aptprice_data) | 기존 부동산 데이터 적재 DB |
| 차트 | Chart.js 4.4.3 (CDN, AJAX) | 경량, 멀티 라인/막대 차트, 이중 Y축 지원 |
| CSS | Bootstrap 5.3.3 (CDN) | 빠른 반응형 레이아웃 구성 |
| AI | Gemini REST API (java.net.http.HttpClient) | 경제 분석 텍스트 + 컷툰 이미지 생성 |
| 외부 데이터 | ECOS API (한국은행) | 경제지표 공식 데이터 소스 |
| 빌드 | Gradle 8.8 | 표준 Java 빌드 도구 |

## 진행 상태 범례

- 완료(DONE)
- 진행 중(IN PROGRESS)
- 예정(TODO)
- 보류(HOLD)

## 의존성 맵

```
Phase 1: 프로젝트 세팅 + 탭 레이아웃
    |
    +---> Phase 2: ECOS API 연동 + 탭1 경제 차트 (8개 섹션)
    |         |
    |         +---> Phase 4: Gemini AI 분석 + 캐싱 + 컷툰
    |
    +---> Phase 3: PostgreSQL 연동 + 탭2 부동산 (KB지수 + 권역별 + TOP5)
              |
              +---> Phase 4: Gemini AI 분석 + 캐싱 + 컷툰
                        |
                        +---> Phase 5: 통합 QA + 성능 최적화 + 배포
```

## 리스크 및 완화 전략

| 리스크 | 영향도 | 발생 확률 | 완화 전략 |
|--------|--------|-----------|-----------|
| ECOS API 일일 호출 제한 초과 | 높음 | 중간 | 서버 캐시 레이어 도입, 배치 조회 최소화 |
| Gemini nano 이미지 생성 미지원 | 높음 | 중간 | gemini-2.0-flash 등 대안 모델 사전 검증, 이미지 생성 불가 시 텍스트 분석만 제공 |
| PostgreSQL 외부 DB 접속 불안정 | 중간 | 낮음 | 커넥션 풀 타임아웃 설정, 장애 시 캐시 데이터 표시 |
| Chart.js 이중 Y축 렌더링 복잡도 | 낮음 | 높음 | 환율/고용 등 단위 차이 큰 지표는 별도 차트 분리 대안 준비 |
| API Key 미발급/미확인 | 높음 | 중간 | Phase 1에서 API Key 확보 및 환경변수 설정 완료 필수 |

---

## Phase 1: 프로젝트 초기 세팅 및 탭 레이아웃 (Sprint 1 - Week 1~2)

**상태: 완료 (2026-03-14)**

### 목표
Spring Boot 프로젝트 골격 완성, 2탭 레이아웃 UI 구현, 개발 환경 구성. 프론트엔드를 먼저 완성하여 UI 리뷰 후 백엔드 연동 진행.

### 우선순위: Must Have

### 작업 목록

- [x] **P1-1. Gradle 프로젝트 초기화** (복잡도: 낮, 1일)
  - Spring Boot 3.3.2, Java 17+ 프로젝트 생성
  - build.gradle에 의존성 추가: spring-boot-starter-web, spring-boot-starter-thymeleaf, thymeleaf-layout-dialect:3.3.0, spring-boot-starter-data-jpa, querydsl-jpa:5.0.0 (jakarta classifier), postgresql driver
  - application.yml 설정: server.port=8086, server.servlet.context-path=/krEconoMon
  - 프로필 분리: application-local.yml, application-prod.yml

- [x] **P1-2. 환경변수 및 API Key 설정** (복잡도: 낮, 0.5일)
  - ECOS API Key 환경변수: `ECOS_API_KEY`
  - Gemini API Key 환경변수: `GEMINI_API_KEY`
  - PostgreSQL 접속 정보: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
  - .env 파일 + application.yml에서 `${ENV_VAR}` 참조 방식 또는 Spring Config 활용

- [x] **P1-3. Thymeleaf 레이아웃 구성** (복잡도: 중, 2일)
  - Layout Dialect 기반 공통 레이아웃 (header, footer, content 영역)
  - Bootstrap 5.3.3 CDN 연결
  - Chart.js 4.4.3 CDN 연결
  - 탭 UI 구현: 탭1(한국 경제 현황), 탭2(서울 부동산 현황)
  - Bootstrap nav-tabs 또는 pills 활용, 탭 전환 시 AJAX 로딩 구조 설계

- [x] **P1-4. 탭1 섹션 레이아웃 (목업)** (복잡도: 중, 2일)
  - 8개 섹션 빈 카드 레이아웃 배치: 금리, GDP/성장률, 환율, 물가, 무역/경상수지, 고용/경기, 통화/유동성, 인구/출산율/고령화
  - 각 섹션에 차트 placeholder (로딩 스피너)
  - 하단 Gemini 분석 결과 영역 placeholder

- [x] **P1-5. 탭2 섹션 레이아웃 (목업)** (복잡도: 중, 2일)
  - 최상단 KB 지수 차트 영역
  - 4개 권역 탭/버튼 (강남권, 강동권, 강서권, 강북권)
  - 권역 선택 시 3개 구 데이터 표시 영역 (차트 + TOP5 테이블)
  - 면적 타입 선택 드롭다운 (UA04: 84m2, UA02: 59m2)
  - 하단 Gemini 분석 결과 영역 placeholder

- [x] **P1-6. Controller 및 라우팅 기본 구조** (복잡도: 낮, 1일)
  - MainController: 메인 페이지 렌더링
  - EconomyApiController: 탭1 AJAX 엔드포인트 (REST)
  - RealEstateApiController: 탭2 AJAX 엔드포인트 (REST)
  - GeminiApiController: AI 분석 엔드포인트 (REST)
  - 공통 응답 DTO 구조 설계

### 완료 기준 (Definition of Done)

- `./gradlew bootRun` 실행 시 http://localhost:8086/krEconoMon 접속 가능
- 탭1/탭2 전환이 정상 동작하며 각 섹션의 placeholder가 표시됨
- Bootstrap 반응형 레이아웃이 모바일/데스크톱에서 정상 렌더링
- API Key 환경변수가 설정되어 있고 Spring에서 주입 가능

### Playwright MCP 검증 시나리오

> `./gradlew bootRun` 실행 후 아래 순서로 검증

**메인 페이지 접속 검증:**
1. `browser_navigate` -> `http://localhost:8086/krEconoMon` 접속
2. `browser_snapshot` -> 페이지 렌더링 상태 확인, 탭1/탭2 탭 요소 존재 확인
3. `browser_console_messages(level: "error")` -> 콘솔 에러 없음 확인

**탭 전환 검증:**
1. `browser_click` -> 탭2(서울 부동산 현황) 탭 클릭
2. `browser_snapshot` -> 탭2 콘텐츠 영역 활성화 확인 (KB지수 차트 placeholder, 4개 권역 탭 존재)
3. `browser_click` -> 탭1(한국 경제 현황) 탭 클릭
4. `browser_snapshot` -> 탭1 콘텐츠 영역 활성화 확인 (8개 섹션 카드 존재)

**반응형 레이아웃 검증:**
1. `browser_resize` -> 모바일 뷰포트 (375x812)
2. `browser_snapshot` -> 모바일 레이아웃 정상 렌더링 확인
3. `browser_resize` -> 데스크톱 뷰포트 (1920x1080)
4. `browser_snapshot` -> 데스크톱 레이아웃 정상 렌더링 확인

**공통 검증:**
- `browser_console_messages(level: "error")` -> 에러 없음 확인
- `browser_network_requests` -> 정적 리소스(Bootstrap CDN, Chart.js CDN) 로딩 200 확인

### 기술 고려사항

- Thymeleaf Layout Dialect의 `layout:decorate` / `layout:fragment` 패턴 사용
- 탭 전환은 클라이언트 사이드(Bootstrap JS)로 처리, 각 탭 콘텐츠는 AJAX lazy loading 방식 권장
- Chart.js와 AJAX 데이터 로딩 패턴은 공통 JS 유틸로 추출 (중복 코드 방지)

---

## Phase 2: ECOS API 연동 및 탭1 경제 차트 (Sprint 2~3 - Week 3~6)

### 목표
ECOS API에서 경제지표 데이터를 조회하고, 8개 섹션별 Chart.js 차트를 렌더링. 데이터 조회 기준(연/분기/월/일)에 맞게 최근 10년치 또는 전년도~현재 데이터 표시.

### 우선순위: Must Have

### 작업 목록

#### Sprint 2 (Week 3~4): ECOS API 서비스 + 섹션 1~4 — 완료 (2026-03-14)

- [x] **P2-1. ECOS API 클라이언트 서비스 구현** (복잡도: 중, 2일)
  - `EcosApiService` 클래스: `java.net.http.HttpClient` 기반 REST 호출
  - 요청 파라미터 빌더: 통계코드, 주기(A/Q/M/D), 시작일, 종료일, 항목코드
  - 응답 JSON 파싱 (Jackson ObjectMapper)
  - 조회 기준 자동 계산: 연/분기/월 -> 최근 10년, 일 -> 전년도 1/1 ~ 현재
  - API 호출 에러 처리 및 재시도 로직 (최대 3회)
  - 호출 빈도 제한 (rate limiting) 고려

- [x] **P2-2. 경제지표 데이터 DB 저장 레이어** (복잡도: 중, 2일)
  - 경제지표 테이블 설계 (JPA Entity): `economy_indicator` (stat_code, item_code, period, value, updated_at)
  - ECOS API 조회 결과를 DB에 저장 (upsert)
  - DB에 데이터가 있으면 DB에서 조회, 없거나 오래된 경우 API 호출 후 갱신
  - Spring Data JPA Repository 구현

- [x] **P2-3. 섹션1 - 금리 차트** (복잡도: 중, 1.5일)
  - 데이터: 한국은행 기준금리(722Y001/M/0101000) + 국고채 3년(817Y002/D/010200000) + 국고채 10년(817Y002/D/010210000)
  - 멀티 라인 차트 1개: 3개 지표를 하나의 Chart.js 라인 차트에 표현
  - 일별 데이터와 월별 데이터 시간축 통합 처리
  - AJAX 엔드포인트: `GET /api/economy/interest-rate`

- [x] **P2-4. 섹션2 - GDP/성장률 차트** (복잡도: 중, 1.5일)
  - 데이터: GDP 성장률(902Y015/Q/KOR) + 1인당 GDP(902Y018/A/KOR)
  - GDP 성장률: 막대 차트 (양수=파랑, 음수=빨강 색상 구분)
  - 1인당 GDP: 라인 또는 막대 차트 (별도)
  - AJAX 엔드포인트: `GET /api/economy/gdp`

- [x] **P2-5. 섹션3 - 환율 차트** (복잡도: 높, 2일)
  - 데이터: USD(731Y001/D/0000001), EUR(0000003), JPY 100엔(0000002), CNY(0000053)
  - 4개 통화 멀티 라인 차트: 이중 Y축 처리 (JPY 100엔 단위 차이 대응)
  - Chart.js `scales` 옵션으로 좌/우 Y축 설정
  - AJAX 엔드포인트: `GET /api/economy/exchange-rate`

- [x] **P2-6. 섹션4 - 물가 차트** (복잡도: 중, 1일)
  - 데이터: CPI(901Y009/M/0) + PPI(404Y014/M/*AA)
  - CPI + PPI 멀티 라인 차트 1개
  - AJAX 엔드포인트: `GET /api/economy/price-index`

#### Sprint 3 (Week 5~6): 섹션 5~8 + 차트 공통 유틸

- [x] **P2-7. 섹션5 - 무역/경상수지 차트** (복잡도: 중, 1.5일)
  - 데이터: 경상수지(301Y013/M/000000) + 수출금액(901Y118/M/T002) + 수입금액(901Y118/M/T004)
  - 경상수지: 막대 차트 (양/음 색상 구분)
  - 수출/수입: 멀티 라인 차트 (별도 또는 이중 Y축)
  - AJAX 엔드포인트: `GET /api/economy/trade`

- [x] **P2-8. 섹션6 - 고용/경기 차트** (복잡도: 중, 1.5일)
  - 데이터: 실업률(901Y027/M/I61BC) + 취업자수(901Y027/M/I61BA)
  - 실업률: 라인 차트, 취업자수: 막대 또는 라인 차트
  - 단위 상이하므로 별도 차트 또는 이중 Y축 처리
  - AJAX 엔드포인트: `GET /api/economy/employment`

- [x] **P2-9. 섹션7 - 통화/유동성 차트** (복잡도: 중, 1.5일)
  - 데이터: M2 광의통화(902Y005/M/KR), 외환보유액 한국(902Y014/M/KR), 일본(JP), 중국(CN)
  - M2: 단독 라인 차트
  - 외환보유액 3개국: 멀티 라인 차트 1개
  - AJAX 엔드포인트: `GET /api/economy/liquidity`

- [x] **P2-10. 섹션8 - 인구/출산율/고령화 차트** (복잡도: 중, 2일)
  - 데이터: 추계인구 전체/남/여(901Y028/A/I35A,I35B,I35C), 고령인구비율(I35D), 합계출산율(I35E)
  - 추계인구: 누적 막대 차트 (남/여 구분)
  - 고령인구비율: 라인 차트 (이중 Y축으로 인구 차트와 결합 가능)
  - 합계출산율: 별도 라인 차트
  - AJAX 엔드포인트: `GET /api/economy/population`

- [x] **P2-11. Chart.js 공통 유틸리티 JS** (복잡도: 중, 1일)
  - 차트 생성 팩토리 함수: `createLineChart()`, `createBarChart()`, `createDualAxisChart()`
  - 공통 옵션: 반응형, 툴팁, 범례, 색상 팔레트
  - AJAX 데이터 로딩 + 로딩 스피너 + 에러 표시 공통 처리
  - 양/음 색상 구분 막대 차트 플러그인

### 완료 기준 (Definition of Done)

- 탭1의 8개 섹션 모두에서 실제 ECOS API 데이터 기반 차트가 렌더링됨
- 멀티 라인 차트에서 각 지표가 범례와 함께 구분 표시됨
- 이중 Y축이 필요한 차트(환율, 고용 등)에서 단위가 올바르게 표시됨
- 막대 차트에서 양/음 값이 색상으로 구분됨
- 데이터 로딩 중 스피너 표시, 에러 시 사용자 친화적 메시지 표시
- ECOS API 응답이 DB에 캐싱되어 재조회 시 API를 재호출하지 않음

### Playwright MCP 검증 시나리오

> `./gradlew bootRun` 실행 후 아래 순서로 검증

**ECOS API 데이터 로딩 검증:**
1. `browser_navigate` -> `http://localhost:8086/krEconoMon` 접속
2. `browser_snapshot` -> 탭1 활성 상태에서 8개 섹션 카드 확인
3. `browser_wait_for` -> 차트 canvas 요소 렌더링 완료 대기
4. `browser_snapshot` -> 각 섹션의 차트가 데이터와 함께 렌더링되었는지 확인

**개별 섹션 차트 검증 (금리 예시):**
1. `browser_snapshot` -> 금리 섹션에서 "기준금리", "국고채 3년", "국고채 10년" 범례 텍스트 존재 확인
2. `browser_network_requests` -> `/api/economy/interest-rate` 호출 200 응답 확인

**전체 섹션 API 호출 검증:**
1. `browser_network_requests` -> 아래 엔드포인트 모두 200 응답 확인:
   - `/api/economy/interest-rate`
   - `/api/economy/gdp`
   - `/api/economy/exchange-rate`
   - `/api/economy/price-index`
   - `/api/economy/trade`
   - `/api/economy/employment`
   - `/api/economy/liquidity`
   - `/api/economy/population`

**에러 상태 검증:**
1. `browser_console_messages(level: "error")` -> 콘솔 에러 없음 확인

### 기술 고려사항

- ECOS API 응답 형식: JSON, `StatisticSearch` 엔드포인트 사용
- 일별(D) 데이터는 데이터 포인트가 많으므로 Chart.js `decimation` 플러그인 또는 서버 사이드 샘플링 고려
- 환율 차트 이중 Y축: JPY(100엔 단위)와 나머지 통화 단위 차이가 크므로 좌축(USD/EUR/CNY)/우축(JPY) 분리
- API 호출 실패 시 사용자에게 "데이터를 불러올 수 없습니다" 메시지 표시 (차트 영역)

---

## Phase 3: PostgreSQL 연동 및 탭2 부동산 현황 (Sprint 3~4 - Week 5~8)

### 목표
PostgreSQL DB에서 서울 부동산 데이터 조회, KB 지수 차트 + 4개 권역별 매매/전세 통계 차트 + TOP5 아파트 테이블 구현. Phase 2와 병렬 진행 가능 (팀원 분리 배정).

### 우선순위: Must Have

### 작업 목록

#### Sprint 3 (Week 5~6): DB 연동 + KB지수 + 권역별 차트

- [x] **P3-1. PostgreSQL JPA 엔티티 및 Repository 구현** (복잡도: 중, 2일)
  - 엔티티 매핑: `StatSigunguYymm`, `StatLeaseSigungu`, `RankUatypeSigungu`, `RankUatypeSigunguLease`
  - QueryDSL Q클래스 생성 설정 (build.gradle annotationProcessor)
  - 각 테이블 Repository 인터페이스 + QueryDSL CustomRepository 구현
  - DB 함수 `get_code_name()` 호출 방안: Native Query 또는 DB View 활용

- [x] **P3-2. 서울 아파트 KB 지수 차트** (복잡도: 중, 1.5일)
  - ECOS API 조회: 매매지수(901Y062/M/P63ACA), 전세지수(901Y063/M/P64ACA)
  - 최근 10년치 월별 데이터
  - 매매지수 + 전세지수 멀티 라인 차트 1개 (탭2 최상단 배치)
  - AJAX 엔드포인트: `GET /api/real-estate/kb-index`

- [x] **P3-3. 권역별 매매가 월별 통계 차트** (복잡도: 중, 2일)
  - QueryDSL 동적 쿼리: `stat_sigungu_yymm` 테이블에서 구코드, 면적타입 기준 조회
  - 4개 권역별 3개 구 데이터: 강남권(강남/서초/송파), 강동권(강동/노원/성동), 강서권(강서/영등포/양천), 강북권(종로/마포/용산)
  - 각 권역 선택 시 3개 구의 avg_price 멀티 라인 차트 (10년치)
  - 면적타입 선택(UA04/UA02) 시 동적 데이터 갱신
  - AJAX 엔드포인트: `GET /api/real-estate/price?region={region}&areaType={areaType}`

- [x] **P3-4. 권역별 전세가 월별 통계 차트** (복잡도: 중, 1.5일)
  - QueryDSL 동적 쿼리: `stat_lease_sigungu` 테이블에서 rent_gbn='0', 구코드, 면적타입 기준 조회
  - 매매가 차트와 동일한 구조로 avg_deposit 멀티 라인 차트
  - AJAX 엔드포인트: `GET /api/real-estate/lease?region={region}&areaType={areaType}`

#### Sprint 4 (Week 7~8): TOP5 테이블 + UI 통합 — 완료 (2026-03-14)

- [x] **P3-5. 올해 매매 TOP5 아파트 테이블** (복잡도: 중, 1.5일)
  - `rank_uatype_sigungu` 테이블: deal_year='2026', rank_type=0, 구코드, 면적타입 기준 조회
  - avg_price DESC 상위 5건
  - Bootstrap 테이블 렌더링: 아파트명, 위치(구명+동), 건축연도, 최저가/평균가/최고가(세로 표시), 거래건수
  - AJAX 엔드포인트: `GET /api/real-estate/top5/trade?sigunguCode={code}&areaType={areaType}`

- [x] **P3-6. 올해 전세 TOP5 아파트 테이블** (복잡도: 중, 1일)
  - `rank_uatype_sigungu_lease` 테이블: deal_year='2026', rank_type=0, rent_gbn='0', 구코드, 면적타입 기준 조회
  - avg_deposit DESC 상위 5건
  - 매매 TOP5와 동일 테이블 구조 (최저/평균/최고 보증금)
  - AJAX 엔드포인트: `GET /api/real-estate/top5/lease?sigunguCode={code}&areaType={areaType}`

- [x] **P3-7. 권역 탭 UI 및 데이터 연동 통합** (복잡도: 중, 2일)
  - 4개 권역 탭 클릭 시 해당 권역의 3개 구 데이터 AJAX 로딩
  - 권역별 구코드 매핑 (프론트 JS 또는 서버 Config)
  - 면적타입 드롭다운 변경 시 차트 + 테이블 동시 갱신
  - 로딩 상태 표시, 에러 처리
  - 각 구별 차트 + TOP5 테이블 레이아웃 정리

### 완료 기준 (Definition of Done)

- 탭2 최상단에 KB 매매/전세지수 차트가 실제 데이터로 렌더링됨
- 4개 권역 탭 전환 시 해당 3개 구의 매매가/전세가 차트가 갱신됨
- 면적타입(UA04/UA02) 선택 변경 시 모든 차트 및 테이블이 동적 갱신됨
- 각 구별 매매/전세 TOP5 테이블에 올바른 데이터 표시됨
- PostgreSQL 쿼리 응답 시간이 2초 이내

### Playwright MCP 검증 시나리오

> `./gradlew bootRun` 실행 후 아래 순서로 검증

**KB 지수 차트 검증:**
1. `browser_navigate` -> `http://localhost:8086/krEconoMon` 접속
2. `browser_click` -> 탭2(서울 부동산 현황) 클릭
3. `browser_snapshot` -> KB 매매지수/전세지수 차트 캔버스 렌더링 확인
4. `browser_network_requests` -> `/api/real-estate/kb-index` 호출 200 확인

**권역별 데이터 전환 검증:**
1. `browser_click` -> "강남권" 탭 클릭
2. `browser_wait_for` -> 차트 로딩 완료 대기
3. `browser_snapshot` -> 강남구/서초구/송파구 데이터 범례 존재 확인
4. `browser_click` -> "강서권" 탭 클릭
5. `browser_wait_for` -> 차트 로딩 완료 대기
6. `browser_snapshot` -> 강서구/영등포구/양천구 데이터 범례 존재 확인

**면적타입 변경 검증:**
1. `browser_select_option` -> 면적타입 드롭다운에서 "UA02 (59m2)" 선택
2. `browser_wait_for` -> 데이터 갱신 완료 대기
3. `browser_snapshot` -> 차트 및 테이블 데이터가 UA02 기준으로 갱신 확인
4. `browser_network_requests` -> `areaType=UA02` 파라미터 포함된 API 호출 확인

**TOP5 테이블 검증:**
1. `browser_snapshot` -> 매매 TOP5 테이블에 5건 데이터 행 존재 확인
2. `browser_snapshot` -> 전세 TOP5 테이블에 5건 데이터 행 존재 확인

**공통 검증:**
- `browser_console_messages(level: "error")` -> 에러 없음 확인
- `browser_network_requests` -> 모든 `/api/real-estate/*` 호출 200 확인

### 기술 고려사항

- QueryDSL BooleanBuilder로 동적 WHERE 조건 구성 (권역/구코드/면적타입)
- `get_code_name()` DB 함수: Native Query `@Query(nativeQuery=true)` 사용, 또는 코드 테이블을 JPA Entity로 매핑하여 JOIN
- TOP5 쿼리의 `LIMIT 5`는 Pageable(PageRequest.of(0, 5))로 처리
- 대량 데이터(10년치 월별 x 3개 구) 조회 시 인덱스 확인: `(sigungu_code, use_area_type, deal_yymm)` 복합 인덱스 권장

---

## Phase 4: Gemini AI 분석 및 캐싱 (Sprint 4~5 - Week 7~10)

### 목표
Gemini API를 연동하여 경제/부동산 분석 텍스트 생성 및 컷툰 이미지 생성. 캐시 레이어를 구현하여 불필요한 API 중복 호출을 방지하고, 신규 데이터 발생 시에만 재분석 트리거.

### 우선순위: Should Have (분석 텍스트) + Could Have (컷툰)

### 작업 목록

#### Sprint 4 (Week 7~8): 캐시 레이어 + 분석 텍스트 — 완료 (2026-03-14)

- [x] **P4-1. 캐시 레이어 구현** (복잡도: 중, 2일)
  - 캐시 테이블 설계 (JPA Entity): `analysis_cache` (cache_key, cache_type, content_text, image_url, data_hash, created_at, updated_at)
  - `data_hash`: 분석에 사용된 데이터의 해시값 (MD5/SHA256)
  - 캐시 조회 로직: cache_key로 조회 -> data_hash 비교 -> 동일하면 캐시 반환, 다르면 재생성
  - Spring `@Cacheable` 또는 커스텀 캐시 서비스
  - 캐시 타입: ECONOMY_ANALYSIS, REALESTATE_ANALYSIS, ECONOMY_CARTOON, REALESTATE_CARTOON

- [x] **P4-2. Gemini API 클라이언트 서비스** (복잡도: 중, 2일)
  - `GeminiApiService` 클래스: `java.net.http.HttpClient` 기반 REST 호출
  - 텍스트 생성 메서드: 경제/부동산 데이터를 프롬프트로 전달하여 분석 텍스트 반환
  - 이미지 생성 메서드: 분석 텍스트를 기반으로 컷툰 이미지 생성 (gemini-2.0-flash 모델)
  - API 호출 타임아웃 설정 (30초), 에러 처리, 재시도 로직
  - 프롬프트 템플릿 관리 (별도 properties 또는 텍스트 파일)

- [x] **P4-3. 탭1 경제 분석 텍스트 생성** (복잡도: 중, 2일)
  - 8개 섹션 데이터를 수집하여 Gemini 프롬프트 구성
  - 프롬프트: "다음 한국 경제 지표 데이터를 분석하여 현재 경제 상황을 500자 이내로 요약해주세요. [데이터]"
  - 분석 결과를 탭1 하단 또는 별도 패널에 표시
  - 캐시 로직 적용: 데이터 해시 비교 -> 변경 시에만 재분석
  - AJAX 엔드포인트: `GET /api/gemini/economy-analysis`

- [x] **P4-4. 탭2 부동산 분석 텍스트 생성** (복잡도: 중, 1.5일)
  - KB 지수 + 권역별 매매/전세 통계를 수집하여 Gemini 프롬프트 구성
  - 프롬프트: "다음 서울 부동산 데이터를 분석하여 현재 서울 부동산 시장 현황을 500자 이내로 요약해주세요. [데이터]"
  - 분석 결과를 탭2 하단에 표시
  - 캐시 로직 적용
  - AJAX 엔드포인트: `GET /api/gemini/realestate-analysis`

#### Sprint 5 (Week 9~10): 컷툰 생성 + 캐싱 정책 완성

- [ ] **P4-5. 경제 컷툰 생성** (복잡도: 높, 2일)
  - Gemini 이미지 생성 API 연동 (gemini-2.0-flash 또는 지원 모델 확인 필요)
  - 분석 텍스트를 기반으로 경제 상황 표현 컷툰 프롬프트 구성
  - 생성된 이미지를 서버 로컬 저장 또는 Base64 캐시
  - 컷툰 형식: 4컷 구성 (기본값, 추후 조정 가능)
  - 이미지 생성 실패 시 분석 텍스트만 표시 (graceful degradation)
  - AJAX 엔드포인트: `GET /api/gemini/economy-cartoon`

- [ ] **P4-6. 부동산 컷툰 생성** (복잡도: 높, 1.5일)
  - P4-5와 동일 구조로 부동산 컷툰 생성
  - AJAX 엔드포인트: `GET /api/gemini/realestate-cartoon`

- [ ] **P4-7. 캐싱 정책 통합 및 재분석 트리거** (복잡도: 중, 2일)
  - 페이지 로드 시 캐시 유효성 검사 플로우:
    1. 현재 데이터 해시 계산
    2. 캐시 테이블의 data_hash와 비교
    3. 동일 -> 캐시된 분석/컷툰 반환
    4. 다름 -> Gemini API 호출 -> 결과 캐시 갱신 -> 반환
  - 수동 재분석 버튼 UI (강제 재생성)
  - Gemini API 불필요한 중복 호출 방지: 동시 요청 시 lock/debounce 처리

### 완료 기준 (Definition of Done)

- 탭1 하단에 Gemini 경제 분석 텍스트가 표시됨
- 탭2 하단에 Gemini 부동산 분석 텍스트가 표시됨
- 동일 데이터에 대해 페이지 재로드 시 Gemini API를 재호출하지 않고 캐시된 결과 표시
- 새로운 데이터 유입 시 자동으로 재분석이 트리거됨
- 컷툰 이미지가 생성되어 분석 텍스트 옆에 표시됨 (이미지 생성 불가 시 텍스트만 표시)
- API 호출 실패 시 사용자에게 "분석을 불러올 수 없습니다" 메시지 표시

### Playwright MCP 검증 시나리오

> `./gradlew bootRun` 실행 후 아래 순서로 검증

**경제 분석 텍스트 검증:**
1. `browser_navigate` -> `http://localhost:8086/krEconoMon` 접속
2. `browser_wait_for` -> Gemini 분석 영역 로딩 완료 대기 (로딩 스피너 사라짐)
3. `browser_snapshot` -> 탭1 하단에 분석 텍스트 콘텐츠 존재 확인
4. `browser_network_requests` -> `/api/gemini/economy-analysis` 호출 200 확인

**부동산 분석 텍스트 검증:**
1. `browser_click` -> 탭2 클릭
2. `browser_wait_for` -> Gemini 분석 영역 로딩 완료 대기
3. `browser_snapshot` -> 탭2 하단에 분석 텍스트 콘텐츠 존재 확인
4. `browser_network_requests` -> `/api/gemini/realestate-analysis` 호출 200 확인

**캐시 동작 검증:**
1. `browser_navigate` -> 페이지 새로고침
2. `browser_network_requests` -> Gemini API 직접 호출이 발생하지 않고 캐시에서 반환되는지 확인 (응답 시간 비교)

**컷툰 이미지 검증:**
1. `browser_snapshot` -> 컷툰 이미지 요소(img 태그) 존재 확인
2. `browser_take_screenshot` -> 시각적으로 컷툰 이미지가 렌더링되었는지 확인

**공통 검증:**
- `browser_console_messages(level: "error")` -> 에러 없음 확인

### 기술 고려사항

- Gemini API 모델 선택: PRD에 `gemini-3-flash` 명시되어 있으나, 실제 사용 가능 모델 확인 필요 (2026-03 기준 최신 모델 적용)
- 이미지 생성 API가 지원되지 않는 모델인 경우: 텍스트 분석만 제공하고, 컷툰은 Phase 5에서 대안 검토
- 프롬프트 엔지니어링: 경제 분석 품질 확보를 위해 프롬프트에 구체적 지시 포함 (수치 인용, 추세 분석, 전망 등)
- 캐시 데이터 해시: 분석에 사용되는 주요 지표의 최신 값들을 직렬화하여 SHA256 해시 생성
- 이미지 저장: 서버 로컬 파일시스템 저장 후 정적 리소스로 서빙, 또는 DB에 Base64 저장

---

## Phase 5: 통합 QA, 성능 최적화 및 배포 (Sprint 5 - Week 9~10)

### 목표
전체 기능 통합 테스트, 성능 최적화, 에러 핸들링 강화, 배포 준비. Phase 4 후반과 병렬 진행.

### 우선순위: Must Have

### 작업 목록

- [ ] **P5-1. 전체 통합 테스트** (복잡도: 중, 2일)
  - 탭1 -> 탭2 전환 시 데이터 로딩 정상 동작 확인
  - 각 API 엔드포인트 단위 테스트 (JUnit 5 + MockMvc)
  - ECOS API 모킹 테스트 (외부 의존성 분리)
  - DB 쿼리 통합 테스트 (TestContainers 또는 H2)
  - 브라우저 호환성 테스트 (Chrome, Firefox, Safari)

- [ ] **P5-2. 성능 최적화** (복잡도: 중, 2일)
  - AJAX 호출 병렬화: 탭1 로드 시 8개 섹션 동시 요청
  - Chart.js 데이터 포인트 최적화: 일별 데이터 샘플링 (1000+ 포인트 제한)
  - DB 쿼리 실행 계획 확인 및 인덱스 최적화
  - Spring Boot actuator 메트릭 확인
  - 정적 리소스 캐싱 헤더 설정
  - Gzip 압축 활성화

- [ ] **P5-3. 에러 핸들링 및 UX 개선** (복잡도: 중, 1.5일)
  - 글로벌 예외 핸들러 (`@ControllerAdvice`)
  - AJAX 에러 시 사용자 친화적 에러 메시지 표시
  - 데이터 없음 상태 처리 (빈 차트 대신 안내 메시지)
  - 로딩 상태 스피너 통일
  - 네트워크 장애 시 재시도 버튼 제공

- [ ] **P5-4. 보안 점검** (복잡도: 낮, 1일)
  - API Key 노출 방지: 서버 사이드에서만 사용, 프론트엔드 미노출
  - SQL Injection 방지: QueryDSL 파라미터 바인딩 확인
  - XSS 방지: Thymeleaf 자동 이스케이프 확인
  - CORS 설정 확인

- [ ] **P5-5. 배포 설정** (복잡도: 중, 1.5일)
  - application-prod.yml 설정 완성
  - Gradle bootJar 빌드 확인
  - Docker 이미지 구성 (선택)
  - 실행 스크립트 작성
  - 로깅 설정 (logback-spring.xml): 파일 로테이션, 레벨 설정

### 완료 기준 (Definition of Done)

- 전체 기능이 에러 없이 동작
- 페이지 초기 로딩 시간 5초 이내 (차트 데이터 포함)
- 모든 API 엔드포인트 응답 시간 3초 이내
- 콘솔 에러/경고 없음
- 보안 취약점 없음
- 프로덕션 빌드 및 실행 가능

### Playwright MCP 검증 시나리오

> 프로덕션 빌드 후 실행: `java -jar build/libs/kr-economy-mon.jar --spring.profiles.active=prod`

**전체 기능 E2E 검증:**
1. `browser_navigate` -> `http://localhost:8086/krEconoMon` 접속
2. `browser_snapshot` -> 탭1 전체 섹션 렌더링 확인
3. `browser_click` -> 탭2 클릭
4. `browser_snapshot` -> KB지수 차트 + 권역 탭 렌더링 확인
5. `browser_click` -> 각 권역 탭 순회 (강남/강동/강서/강북)
6. `browser_snapshot` -> 각 권역의 차트 + TOP5 테이블 확인
7. `browser_select_option` -> 면적타입 UA02 선택
8. `browser_snapshot` -> 데이터 갱신 확인

**성능 검증:**
1. `browser_navigate` -> 페이지 접속 시간 측정
2. `browser_network_requests` -> 전체 API 응답 시간 확인 (3초 이내)
3. `browser_console_messages(level: "error")` -> 에러 없음 확인
4. `browser_console_messages(level: "warning")` -> 경고 최소화 확인

**반응형 최종 검증:**
1. `browser_resize` -> 375x812 (모바일)
2. `browser_snapshot` -> 모바일 레이아웃 차트/테이블 정상 표시 확인
3. `browser_resize` -> 768x1024 (태블릿)
4. `browser_snapshot` -> 태블릿 레이아웃 확인
5. `browser_resize` -> 1920x1080 (데스크톱)
6. `browser_snapshot` -> 데스크톱 레이아웃 확인

**Gemini 분석 통합 검증:**
1. `browser_navigate` -> 탭1 접속
2. `browser_wait_for` -> 분석 텍스트 로딩 완료
3. `browser_snapshot` -> 분석 텍스트 + 컷툰 표시 확인
4. `browser_click` -> 탭2 전환
5. `browser_wait_for` -> 분석 텍스트 로딩 완료
6. `browser_snapshot` -> 분석 텍스트 + 컷툰 표시 확인

### 기술 고려사항

- Spring Boot Actuator: health, metrics, info 엔드포인트 활성화 (프로덕션 모니터링)
- 로그: 구조화된 JSON 로그 형식 권장 (ELK 연동 대비)
- Docker: multi-stage build로 이미지 크기 최소화

---

## 기술 부채 관리

| 항목 | 발생 Phase | 해결 목표 | 설명 |
|------|-----------|-----------|------|
| ECOS API 에러 처리 고도화 | Phase 2 | Phase 5 | 초기에는 기본 재시도만 구현, 추후 circuit breaker 패턴 적용 |
| Chart.js 대규모 데이터 최적화 | Phase 2 | Phase 5 | 일별 데이터 포인트가 많을 경우 decimation 플러그인 적용 |
| DB 쿼리 성능 튜닝 | Phase 3 | Phase 5 | 초기에는 기능 구현 우선, 추후 실행 계획 기반 인덱스 최적화 |
| Gemini 프롬프트 튜닝 | Phase 4 | 지속 | 분석 품질 개선을 위한 프롬프트 반복 개선 |
| 테스트 커버리지 | 전체 | Phase 5+ | 초기에는 핵심 서비스만 테스트, 추후 80%+ 커버리지 목표 |

---

## 마일스톤

| 마일스톤 | 목표일 | Phase | 데모 결과물 |
|----------|--------|-------|-------------|
| M1: 프로젝트 골격 완성 | Week 2 완료 | Phase 1 | 2탭 레이아웃 UI, 탭 전환 동작 |
| M2: 탭1 경제 대시보드 MVP | Week 6 완료 | Phase 2 | 8개 섹션 실제 데이터 차트 렌더링 |
| M3: 탭2 부동산 대시보드 MVP | Week 8 완료 | Phase 3 | KB지수 + 4개 권역 차트 + TOP5 테이블 |
| M4: AI 분석 기능 완성 | Week 10 완료 | Phase 4 | Gemini 분석 텍스트 + 컷툰 + 캐싱 |
| M5: 최종 릴리스 | Week 10 완료 | Phase 5 | 통합 QA 완료, 프로덕션 배포 가능 |

---

## 향후 계획 (Backlog) - MVP 이후

아래 항목들은 PRD 범위 외이며, MVP 완료 후 우선순위를 평가하여 진행합니다.

| 기능 | 우선순위 | 설명 |
|------|----------|------|
| 사용자 인증/권한 | Could Have | 관리자 전용 설정 페이지 (API Key 관리 등) |
| 데이터 자동 갱신 | Should Have | 스케줄러(Cron)를 통한 ECOS API 데이터 자동 수집 |
| 알림 기능 | Could Have | 특정 지표 급변 시 알림 (이메일/Slack) |
| 다크 모드 | Could Have | Bootstrap 다크 테마 토글 |
| PDF 리포트 내보내기 | Could Have | 현재 대시보드 상태를 PDF로 저장 |
| 추가 경제지표 | Could Have | 부동산 외 지역별 데이터, 산업별 지표 등 확장 |
| 차트 커스터마이징 | Won't Have (현재) | 사용자가 차트 종류/색상/기간을 직접 설정 |

---

## 미확정 사항 (착수 전 확인 필요)

PRD 섹션 5에 명시된 미확정 사항을 Phase 1 착수 전에 확인하여 리스크를 최소화해야 합니다.

| # | 항목 | 확인 필요 내용 | 영향 Phase |
|---|------|--------------|-----------|
| 1 | ECOS API 인증키 | API Key 발급 여부 및 환경변수 관리 방식 | Phase 1, 2 |
| 2 | Gemini API 키 | 키 발급 여부, 사용 모델명 확인, 이미지 생성 지원 여부 | Phase 4 |
| 3 | use_area_type 기본값 | UA04 고정 or 사용자 선택 가능 -> 현재 계획은 선택 가능(UA04/UA02) | Phase 3 |
| 4 | 캐시 저장 위치 | 현재 계획: 서버 DB (analysis_cache 테이블) | Phase 4 |
| 5 | 환율 차트 이중 Y축 | JPY 100엔 단위 처리 방식 -> 현재 계획: 이중 Y축 | Phase 2 |
| 6 | 컷툰 형식 | 몇 컷 구성, 스타일 가이드 -> 현재 계획: 4컷 기본 | Phase 4 |
