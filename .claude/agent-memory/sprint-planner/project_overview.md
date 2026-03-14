---
name: project_overview
description: 한국 경제 & 서울 부동산 대시보드 프로젝트의 핵심 컨텍스트 - 기술 스택, 아키텍처, 마일스톤
type: project
---

Spring Boot 3.3.2 + Thymeleaf Layout Dialect 3.3.0 기반 단일 페이지 2탭 대시보드. 서버: `http://localhost:8086/krEconoMon`.

**Why:** ECOS API 경제지표 8개 섹션 + 서울 부동산 4개 권역 데이터를 Gemini AI 분석 및 컷툰과 함께 제공.

**How to apply:** 새 Sprint 계획 수립 시 이 컨텍스트를 기반으로 기술 스택과 아키텍처 결정을 일관성 있게 유지한다.

## 기술 스택
- Backend: Spring Boot 3.3.2 MVC
- Templating: Thymeleaf Layout Dialect 3.3.0 (`layout:decorate` 패턴)
- DB Access: Spring Data JPA + QueryDSL 5.0.0 (Jakarta classifier)
- Database: PostgreSQL (`leeghhome.iptime.org:5432/aptprice_data`)
- Charts: Chart.js 4.4.3 (CDN)
- CSS: Bootstrap 5.3.3 (CDN)
- AI: Gemini REST API (`gemini-2.0-flash`, `java.net.http.HttpClient`)
- Economic Data: ECOS API (`http://ecos.bok.or.kr/api/StatisticSearch`)
- Build: Gradle 8.8, Java 17+

## 패키지 구조
`com.example.kreconomonmon/`
- `controller/`: MainController, EconomyApiController, RealEstateApiController, GeminiApiController
- `service/`: EcosApiService, GeminiApiService, EconomyIndicatorService, RealEstateService, AnalysisCacheService
- `repository/`: Spring Data JPA + QueryDSL repositories
- `entity/`: JPA entities
- `dto/`: Response DTOs

## 마일스톤
- M1: Week 2 완료 - Phase 1 (Sprint 1): 2탭 레이아웃 UI
- M2: Week 6 완료 - Phase 2 (Sprint 2~3): 8개 경제 섹션 차트
- M3: Week 8 완료 - Phase 3 (Sprint 3~4): KB지수 + 권역별 차트 + TOP5 테이블
- M4: Week 10 완료 - Phase 4 (Sprint 4~5): Gemini AI 분석 + 컷툰 + 캐싱
- M5: Week 10 완료 - Phase 5 (Sprint 5): 통합 QA + 배포
