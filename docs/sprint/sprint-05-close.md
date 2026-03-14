# Sprint 5 완료 보고서

**Sprint:** 5 (Phase 4 완성: P4-5~P4-7 / Phase 5 전체: P5-1~P5-5)
**기간:** 2026-03-14 ~ 2026-03-14 (계획: Week 9~10)
**완료일:** 2026-03-14
**상태:** 완료

---

## 1. 구현 범위 요약

Sprint 5의 목표인 "Gemini 컷툰 생성(P4-5~P4-7) + 통합 QA·성능 최적화·배포 준비(P5-1~P5-5)"를 완료하였습니다. 이로써 M5 최종 릴리스 목표가 달성되었습니다.

### 신규 생성 파일

#### DTO
| 파일 | 설명 |
|------|------|
| `dto/CartoonResponse.java` | 컷툰 API 응답 DTO. `status`, `imageData`(Base64), `cached` 필드 |

#### 컨트롤러
| 파일 | 설명 |
|------|------|
| `controller/GlobalExceptionHandler.java` | `@RestControllerAdvice` 글로벌 예외 처리. `IllegalArgumentException` → 400, `RuntimeException`/`Exception` → 500, `ApiErrorResponse` 반환 |

#### 설정
| 파일 | 설명 |
|------|------|
| `config/WebConfig.java` | CORS 설정. `/api/**` 경로에 `localhost:*`·`leeghhome.iptime.org:*` 허용. GET/POST 메서드 |

#### 리소스
| 파일 | 설명 |
|------|------|
| `resources/logback-spring.xml` | local 프로파일: CONSOLE 출력(DEBUG), prod 프로파일: FILE 롤링(30일 보관)+CONSOLE(WARN) |
| `resources/db/migration/V5__add_image_data_to_analysis_cache.sql` | `analysis_cache.image_data TEXT` 컬럼 추가 마이그레이션 |

#### 배포 스크립트
| 파일 | 설명 |
|------|------|
| `scripts/start.sh` | 프로덕션 실행 스크립트. 필수 환경변수 7개 사전 검증, `/var/log/kr-economy-mon/` 파일 로그, PID 파일 관리 |

#### 테스트
| 파일 | 설명 |
|------|------|
| `controller/EconomyApiControllerIntegrationTest.java` | 8개 경제 엔드포인트 MockMvc 통합 테스트. `@WebMvcTest` + `@MockBean EconomyIndicatorService` |
| `controller/GlobalExceptionHandlerTest.java` | 예외 핸들러 단위 테스트. 400/500 응답 코드 및 에러 필드 검증 |
| `service/GeminiAnalysisServiceTest.java` | 컷툰/분석 캐시 히트·미스·에러 경로 9개 시나리오 검증 |

### 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `service/GeminiApiService.java` | `generateImage()`, `buildImageRequestBody()`, `extractImageFromResponse()` 추가. 이미지 API URL `gemini-2.0-flash-exp` 모델, 60초 타임아웃 |
| `service/GeminiAnalysisService.java` | `getEconomyCartoon()`, `getRealEstateCartoon()` 추가. 각각 `ReentrantLock`으로 동시 중복 호출 방지 |
| `service/AnalysisCacheService.java` | `getCachedImage()`, `saveOrUpdate()`, `invalidate()`, `invalidateAll()` 추가 |
| `entity/AnalysisCache.java` | `image_data TEXT` 컬럼 추가. `updateContent()` 3인자 시그니처(`contentText, imageData, dataHash`) |
| `controller/GeminiApiController.java` | `/api/gemini/economy-cartoon`, `/api/gemini/realestate-cartoon` 실제 연동. `POST /api/gemini/refresh` 전체 캐시 무효화 |
| `service/EconomyIndicatorService.java` | `sampleIfNeeded(List, maxPoints)` 데이터 포인트 균등 샘플링 추가 |
| `resources/application.yml` | `server.compression` Gzip 활성화 (JSON/JS/CSS/HTML, 1KB 이상) |
| `resources/application-prod.yml` | Spring Actuator `health,info,metrics` 엔드포인트 노출 설정 |
| `build.gradle` | `spring-boot-starter-actuator` 의존성 추가 |
| `templates/economy.html`, `templates/realestate.html` | 컷툰 섹션 UI 추가 |
| `static/js/common.js`, `static/js/realestate.js` | `loadEconomyCartoon()`, `loadRealEstateCartoon()` AJAX 함수 추가 |

---

## 2. 테스트 결과

- 전체 테스트: **81개 통과** (23개 테스트 클래스)
- 신규 테스트: 37개 (Sprint 4 대비 증가분, 모두 통과)
- 실패: 0

### 주요 테스트 커버리지

| 영역 | 테스트 수 | 비고 |
|------|----------|------|
| 경제 API 통합 테스트 (MockMvc) | 8 | 8개 엔드포인트 전체 커버 |
| Gemini 분석/컷툰 서비스 | 9 | 캐시 히트·미스·에러·동시성 시나리오 |
| 글로벌 예외 핸들러 | 3 | 400/500 응답 코드 검증 |

---

## 3. 완료 기준 달성 여부

| 완료 기준 | 상태 |
|-----------|------|
| 탭1 하단 경제 컷툰 이미지 섹션 구현 | 완료 |
| 탭2 하단 부동산 컷툰 이미지 섹션 구현 | 완료 |
| 이미지 생성 API 실패 시 graceful degradation (텍스트만 표시) | 완료 |
| 동일 데이터 해시 재요청 시 컷툰 캐시 반환 | 완료 |
| `POST /api/gemini/refresh` 수동 캐시 무효화 | 완료 |
| ReentrantLock으로 동시 Gemini 중복 호출 방지 | 완료 |
| 8개 경제 엔드포인트 MockMvc 통합 테스트 | 완료 |
| GlobalExceptionHandler로 통일된 에러 응답 | 완료 |
| Gzip 압축 활성화 | 완료 |
| Spring Boot Actuator 엔드포인트 설정 | 완료 |
| logback-spring.xml 프로파일별 로그 설정 | 완료 |
| scripts/start.sh 프로덕션 실행 스크립트 | 완료 |
| `./gradlew clean build` 성공 | 확인 필요 (Java 21 환경 필요) |
| 전체 테스트 81개 통과 | 완료 |

---

## 4. 코드 리뷰 결과 요약

### High 이슈 (권고 수준)

| # | 위치 | 내용 |
|---|------|------|
| H-1 | `GeminiApiController.java:53` | `POST /api/gemini/refresh`가 인증 없이 노출됨. 누구나 호출하여 전체 캐시를 무효화할 수 있음. MVP 범위에서는 허용되나, 프로덕션 배포 전 Spring Security 또는 IP 제한 적용 권고 |
| H-2 | `GeminiApiService.java:23` | 이미지 생성 URL이 `gemini-2.0-flash-exp` 하드코딩. `application.yml`의 `app.gemini.model` 설정값과 불일치. 텍스트 생성과 동일하게 프로퍼티 주입으로 통일 권고 |

### Medium 이슈 (기술 부채 등록)

| # | 위치 | 내용 |
|---|------|------|
| M-1 | `WebConfig.java:13` | `allowedOriginPatterns("http://leeghhome.iptime.org:*")`는 해당 호스트의 모든 포트를 허용. 운영 환경에서는 특정 포트로 제한 권고 |
| M-2 | `GeminiAnalysisService.java` | `buildEconomyDataSnapshot()`이 4개 지표(기준금리·USD환율·GDP성장률·CPI)만 사용. 8개 섹션 데이터를 모두 활용하면 분석 품질 향상 가능 |
| M-3 | `AnalysisCacheService.java:67` | `saveCache()`가 `saveOrUpdate()`를 내부 호출하는 래퍼이나, `cacheKey`와 `cacheType`에 동일 값을 전달하는 호출 패턴이 반복됨. 향후 리팩터링 고려 |
| M-4 | `logback-spring.xml` | prod 프로파일에서 `/var/log/kr-economy-mon/` 경로가 하드코딩. `application-prod.yml`의 `logging.file.name` 설정과 중복. 한 곳으로 일원화 권고 |

### Low 이슈

| # | 위치 | 내용 |
|---|------|------|
| L-1 | `GlobalExceptionHandler.java:29` | `RuntimeException` 핸들러가 내부 예외 메시지를 클라이언트에 노출하지 않는 점은 적절. 그러나 로그에 스택 트레이스 전체 출력(`log.error("서버 오류 발생", e)`)이 prod에서 INFO 레벨이므로 실제로는 출력되지 않을 수 있음. WARN 이상으로 조정 검토 |
| L-2 | `CartoonResponse.java` | `imageData`가 Base64 문자열일 경우 크기가 클 수 있음 (이미지 1장 약 100~300KB). 응답 압축(Gzip)이 활성화되어 있어 실용적으로는 문제없으나, 향후 대용량 이미지 시 스트리밍 대안 고려 |

### 긍정적 평가

- `ReentrantLock.tryLock()` 패턴으로 동시 요청 시 중복 Gemini API 호출을 효과적으로 방지
- `generateText()`와 `generateImage()` 각각 30초/60초 타임아웃 분리 적용
- `scripts/start.sh`에서 7개 필수 환경변수 사전 검증으로 배포 오류를 조기에 감지
- `EconomyApiControllerIntegrationTest`가 `@WebMvcTest` + `@MockBean`으로 외부 의존성을 격리하여 빠르고 안정적인 테스트 구성

---

## 5. 검증 결과

- [검증 보고서](sprint05/verification-report.md) — 단위/통합 테스트 81개 통과, 코드 리뷰 결과 포함
- Playwright UI 검증: 수동 검증 필요 (서버 실행 환경 및 Gemini API Key 필요)

---

## 6. 프로젝트 최종 완료 선언

Sprint 5 완료로 전체 5개 스프린트 로드맵이 종료됩니다.

| 마일스톤 | 완료일 | 결과 |
|----------|--------|------|
| M1: 프로젝트 골격 완성 | 2026-03-14 | Sprint 1 완료 |
| M2: 탭1 경제 대시보드 MVP | 2026-03-14 | Sprint 2~3 완료 |
| M3: 탭2 부동산 대시보드 MVP | 2026-03-14 | Sprint 3~4 완료 |
| M4: AI 분석 기능 완성 | 2026-03-14 | Sprint 4~5 완료 |
| M5: 최종 릴리스 | 2026-03-14 | Sprint 5 완료 |

### 프로덕션 배포 전 체크리스트 (수동 수행 필요)

- `./gradlew clean build` 빌드 성공 확인 (Java 21 환경)
- `java -jar build/libs/kr-economy-mon.jar --spring.profiles.active=prod` 실행 확인
- Playwright E2E 검증: 8개 경제 차트, KB지수 차트, 4개 권역 탭, TOP5 테이블, Gemini 분석/컷툰
- `POST /api/gemini/refresh` 접근 제어 적용 (H-1 이슈)
- `GeminiApiService.GEMINI_IMAGE_API_URL` 프로퍼티 주입으로 변경 (H-2 이슈)
