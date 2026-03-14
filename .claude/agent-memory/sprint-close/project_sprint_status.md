---
name: 스프린트 진행 현황
description: 각 Sprint의 완료 여부 및 브랜치 정보
type: project
---

Sprint 1 (Phase 1: 프로젝트 초기 세팅 및 탭 레이아웃) — 완료 (2026-03-14)
- 브랜치: `main_sprint1`
- 커밋: ddf44d7
- 완료 보고서: `docs/sprint/sprint-01-close.md`

Sprint 2 (Phase 2 부분: P2-1~P2-6 ECOS API 연동 + 경제지표 4개 차트) — 완료 (2026-03-14)
- 브랜치: `main_sprint02`
- 커밋: b22fc5c (구현), 3afae9b (완료 보고서)
- 완료 보고서: `docs/sprint/sprint-02-close.md`
- PR: https://github.com/leegh-gc/kr-economy-mon/compare/main...main_sprint02 (미머지 상태)
- 미구현(Sprint 3 예정): P2-7~P2-10 (무역/고용/통화/인구), P2-11 Chart.js 유틸 고도화

**Why:** ECOS API 클라이언트(EcosApiService), DB 캐시(EconomyIndicator JPA), 4개 차트 엔드포인트 실구현.

**How to apply:** Sprint 3 착수 시 `main_sprint02` 브랜치 기반으로 새 브랜치 생성. Java 21 설치 및 `./gradlew test` 통과 확인 후 착수.
