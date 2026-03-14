---
name: sprint_history
description: 완료/진행 중인 Sprint 번호와 주요 달성 사항 추적
type: project
---

## Sprint 현황 (2026-03-14 기준)

Sprint 1 완료. Sprint 2 완료 (P2-1~P2-6: ECOS API 클라이언트, EconomyIndicator JPA, 금리/GDP/환율/물가 4개 차트).
Sprint 3 계획 수립 완료 (2026-03-14). 계획 파일: `docs/plans/2026-03-14-sprint-03.md`.

| Sprint | Phase | 기간 | 상태 | 주요 목표 |
|--------|-------|------|------|-----------|
| Sprint 1 | Phase 1 | Week 1~2 (2026-03-14~27) | 완료 | Gradle 초기화, Thymeleaf 레이아웃, 2탭 목업, Controller 기본 구조 |
| Sprint 2 | Phase 2 | Week 3~4 (2026-03-28~04-10) | 완료 | ECOS API 클라이언트, EconomyIndicator JPA, 경제 섹션 1~4 차트(금리/GDP/환율/물가) |
| Sprint 3 | Phase 2+3 | Week 5~6 (2026-04-11~04-24) | 계획 완료 | 경제 섹션 5~8 (P2-7~P2-10) + 부동산 DB 연동 + KB지수 + 권역별 차트 (P3-1~P3-4) |
| Sprint 4 | Phase 3+4 | Week 7~8 (2026-04-25~05-08) | 계획 완료 | TOP5 테이블(P3-5~P3-7) + 캐시 레이어(P4-1) + Gemini 분석 텍스트(P4-2~P4-4) |
| Sprint 5 | Phase 4+5 | Week 9~10 (2026-05-09~05-22) | 계획 완료 | Gemini 컷툰(P4-5~P4-7) + 통합 QA + 성능 최적화 + 배포(P5-1~P5-5) |

## Sprint 2 핵심 기술 결정 사항 (2026-03-14)

- `EcosApiService.buildDateRange()` 를 package-private으로 두어 단위 테스트 가능하게 설계
- `EconomyIndicatorService`: DB 비어 있을 때만 ECOS 재호출 (단순 비어있음 판단, stale 판단은 Sprint 3 이후 개선 예정)
- `ChartDataResponse` DTO: `yAxisID` 필드로 이중 Y축 지원 (환율 차트 JPY 우축)
- application-local.yml DataSource exclude는 Sprint 2 Task 1에서 제거

## Sprint 5 핵심 기술 결정 사항 (2026-03-14)

- 컷툰 이미지 저장: `analysis_cache.image_data` TEXT 컬럼에 Base64 저장 (`image_url` 컬럼 대체) — V5 DDL 마이그레이션
- Gemini 이미지 생성: `responseModalities: ["IMAGE", "TEXT"]` 요청, `extractImageFromResponse()` package-private으로 단위 테스트 가능
- 이미지 생성 불가 시: `Optional.empty()` 반환 → graceful degradation (텍스트만 표시)
- 동시 요청 lock: `ReentrantLock` (경제/부동산 각 1개) — 동시 Gemini 호출 방지
- 수동 재분석: `POST /api/gemini/refresh` → `AnalysisCacheService.invalidateAll()` → hash를 "INVALIDATED"로 교체
- `CartoonResponse` 신규 DTO: `imageData`(String) + `available`(boolean) 필드
- 통합 테스트: `@WebMvcTest` 계층 격리 (TestContainers 미사용 — YAGNI)
- 성능: `Promise.all()` AJAX 병렬화, `sampleIfNeeded(data, 500)` 일별 데이터 샘플링, Gzip 압축
- 배포: `application-prod.yml` `ddl-auto: validate`, Spring Boot Actuator(health/info/metrics), logback 파일 로테이션 30일

## Sprint 4 핵심 기술 결정 사항 (2026-03-14)

- `RankUatypeSigungu` / `RankUatypeSigunguLease` 신규 엔티티 추가 (Sprint 3에서 누락)
- TOP5 쿼리: QueryDSL 불필요, Spring Data JPA 메서드 네이밍 + `Pageable(0, 5)` 로 처리
- `Top5Response` 신규 DTO: 매매/전세 공통 구조, avgDeposit → avgPrice 필드에 매핑
- TOP5 테이블은 권역당 첫 번째 구(대표 구)만 표시 (YAGNI, Sprint 5에서 확장 가능)
- `AnalysisCache` 엔티티: PK = `cache_key`(String), `updateContent()` 가변 메서드 포함
- `AnalysisCacheService.computeHash()`: `java.security.MessageDigest` SHA-256 + `HexFormat` (Java 17+)
- `GeminiApiService`: `java.net.http.HttpClient` 기반, `buildRequestBody()`/`extractTextFromResponse()` package-private으로 테스트 가능
- Gemini 모델: `gemini-2.0-flash` (project_overview.md 기준)
- `GeminiAnalysisService`: 경제/부동산 분석 오케스트레이션, 캐시 미스 시에만 Gemini API 호출
- API Key 미설정 시 graceful degradation — 에러 메시지 반환 (앱 중단 없음)
- `analysis_cache` 테이블 DDL: `src/main/resources/db/migration/V4__create_analysis_cache.sql`

## Sprint 3 핵심 기술 결정 사항 (2026-03-14)

- 경제 섹션 5~8은 기존 `EconomyIndicatorService.getIndicators()` + `EconomyApiController.toDataset()` 패턴 재사용
- 부동산 엔티티: `StatSigunguYymm`, `StatLeaseSigungu` 신규 추가. Q클래스는 `compileJava`로 생성
- `QueryDslConfig`: `JPAQueryFactory` 빈을 별도 Config 클래스에서 등록
- `RealEstateService`: 구코드-구명 매핑을 서비스 내부 상수 Map으로 관리
- 부동산 전용 JS를 `realestate.js`로 분리 (common.js는 경제 탭 전용 유지)
- `/api/real-estate/price|lease` 엔드포인트: `codes` 쿼리 파라미터로 쉼표 구분 구코드 전달
- KB지수는 기존 ECOS API 경로 재사용 (`EconomyIndicatorService`)
- TOP5 테이블은 Sprint 4로 연기 (P3-5~P3-7)

**Why:** Sprint 계획 이력을 추적하여 다음 Sprint 번호 결정과 완료 맥락 파악에 활용.

**How to apply:** 새 Sprint 계획 요청 시 이 테이블에서 다음 Sprint 번호를 확인하고, 이전 Sprint의 완료 여부를 ROADMAP.md와 대조한다.
