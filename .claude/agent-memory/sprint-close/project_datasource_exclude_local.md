---
name: local 프로필 DataSource AutoConfig exclude
description: Sprint 2에서 exclude 제거 완료 - DB 연결 활성화 상태
type: project
---

Sprint 2에서 `application-local.yml`의 DataSource AutoConfig exclude가 제거되어 DB 연결이 활성화됨.

**이전 상태 (Sprint 1):** `spring.autoconfigure.exclude`로 DataSource/JPA/HibernateJpa AutoConfig 3개를 exclude하여 DB 없이 로컬 구동 가능.

**현재 상태 (Sprint 2 이후):** exclude 완전 제거. `application-local.yml`에 PostgreSQL DataSource 설정 활성화. POSTGRES_USER, POSTGRES_PASSWORD 환경변수 필요.

**Why:** Phase 2부터 economy_indicator 테이블 JPA 연동이 필요하여 Sprint 2에서 제거.

**How to apply:** 이 변경은 이미 완료됨. 로컬 개발 시 PostgreSQL 접속 환경변수 설정 필수.
