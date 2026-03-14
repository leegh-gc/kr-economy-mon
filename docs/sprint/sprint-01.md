# Sprint 1: 프로젝트 초기 세팅 및 탭 레이아웃 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Spring Boot 프로젝트 골격을 완성하고, 2탭(경제/부동산) Thymeleaf 레이아웃 UI를 구현하여 `http://localhost:8086/krEconoMon`에서 탭 전환이 동작하는 목업 앱을 완성한다.

**Architecture:** Spring Boot 3.3.2 MVC + Thymeleaf Layout Dialect 3.3.0으로 서버 사이드 렌더링. MainController가 메인 페이지를 렌더링하고, EconomyApiController / RealEstateApiController / GeminiApiController가 각각 탭별 AJAX REST 엔드포인트를 담당한다. 탭 전환은 Bootstrap JS(클라이언트 사이드)로 처리하며, 탭 콘텐츠는 AJAX lazy loading 구조로 설계한다.

**Tech Stack:** Spring Boot 3.3.2, Java 17+, Thymeleaf Layout Dialect 3.3.0, Bootstrap 5.3.3 (CDN), Chart.js 4.4.3 (CDN), Gradle 8.8, PostgreSQL (미연동, Phase 3), QueryDSL 5.0.0 (Jakarta, 의존성만 설정)

**Sprint 기간:** Week 1~2 (2026-03-14 ~ 2026-03-27)

**Sprint 번호:** 1 (Phase 1 대응)

---

## 사전 준비 체크리스트

Sprint 착수 전 아래 항목을 확인한다:

- [ ] Java 17+ 설치 확인: `java -version`
- [ ] Gradle 8.8 설치 확인: `./gradlew --version` (Wrapper 사용 시 자동 다운로드)
- [ ] ECOS API Key 발급 확인 (Phase 2에서 사용, 환경변수 설정만 이번 Sprint에서 완료)
- [ ] Gemini API Key 발급 확인 (Phase 4에서 사용, 환경변수 설정만 이번 Sprint에서 완료)
- [ ] PostgreSQL 접속 정보 확인 (Phase 3에서 사용, 환경변수 설정만 이번 Sprint에서 완료)

---

## Task 1: Gradle 프로젝트 초기화 (P1-1)

**Files:**
- Create: `build.gradle`
- Create: `settings.gradle`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `src/main/resources/application.yml`
- Create: `src/main/java/com/example/kreconomonmon/KrEconoMonApplication.java`

**Step 1: `settings.gradle` 생성**

```groovy
rootProject.name = 'kr-economy-mon'
```

**Step 2: `build.gradle` 생성**

```groovy
plugins {
    id 'org.springframework.boot' version '3.3.2'
    id 'io.spring.dependency-management' version '1.1.6'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Thymeleaf Layout Dialect
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'

    // QueryDSL (Jakarta)
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

    // PostgreSQL Driver
    runtimeOnly 'org.postgresql:postgresql'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

// QueryDSL Q클래스 생성 경로 설정
def generated = 'src/main/generated'

tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(generated))
}

sourceSets {
    main.java.srcDirs += [generated]
}

clean {
    delete file(generated)
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**Step 3: `src/main/resources/application.yml` 생성**

```yaml
server:
  port: 8086
  servlet:
    context-path: /krEconoMon

spring:
  application:
    name: kr-economy-mon
  profiles:
    active: local
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
  jpa:
    open-in-view: false

logging:
  level:
    com.example.kreconomonmon: DEBUG
```

**Step 4: `KrEconoMonApplication.java` 생성**

```
src/main/java/com/example/kreconomonmon/KrEconoMonApplication.java
```

```java
package com.example.kreconomonmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KrEconoMonApplication {

    public static void main(String[] args) {
        SpringApplication.run(KrEconoMonApplication.class, args);
    }
}
```

**Step 5: 빌드 확인**

```bash
./gradlew build -x test
```

기대 출력: `BUILD SUCCESSFUL`

**Step 6: 커밋**

```bash
git add build.gradle settings.gradle src/main/resources/application.yml \
        src/main/java/com/example/kreconomonmon/KrEconoMonApplication.java
git commit -m "feat: initialize Gradle project with Spring Boot 3.3.2"
```

---

## Task 2: Spring 프로필 분리 및 환경변수 설정 (P1-2)

**Files:**
- Create: `src/main/resources/application-local.yml`
- Create: `src/main/resources/application-prod.yml`
- Create: `.gitignore` (환경변수 파일 제외)

**Step 1: `application-local.yml` 생성**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:leeghhome.iptime.org}:${POSTGRES_PORT:5432}/${POSTGRES_DB:aptprice_data}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
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

**Step 2: `application-prod.yml` 생성**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      connection-timeout: 30000
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  thymeleaf:
    cache: true

app:
  ecos:
    api-key: ${ECOS_API_KEY}
    base-url: http://ecos.bok.or.kr/api
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.0-flash
    timeout-seconds: 30

logging:
  level:
    com.example.kreconomonmon: INFO
```

**Step 3: `.gitignore` 생성 (또는 업데이트)**

```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# IDE
.idea/
*.iml
.vscode/

# Environment
.env
*.env
application-secrets.yml

# QueryDSL generated
src/main/generated/

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db
```

**Step 4: 환경변수 설정 검증 테스트 작성**

```
src/test/java/com/example/kreconomonmon/config/AppPropertiesTest.java
```

```java
package com.example.kreconomonmon.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class AppPropertiesTest {

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Test
    void serverPortShouldBe8086() {
        assertThat(serverPort).isEqualTo(8086);
    }

    @Test
    void contextPathShouldBeKrEconoMon() {
        assertThat(contextPath).isEqualTo("/krEconoMon");
    }
}
```

**Step 5: 테스트 실행**

```bash
./gradlew test --tests "com.example.kreconomonmon.config.AppPropertiesTest"
```

기대 출력: `BUILD SUCCESSFUL`, 2 tests passed

> 주의: DB 접속 정보가 없으면 DataSource 자동 설정 실패로 테스트가 실패할 수 있다.
> 이 경우 테스트 클래스에 `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)`을 추가하거나,
> `@TestPropertySource(properties = {"spring.datasource.url=", "spring.jpa.hibernate.ddl-auto=none"})`으로 DB 설정을 비활성화한다.

**Step 6: 커밋**

```bash
git add src/main/resources/application-local.yml \
        src/main/resources/application-prod.yml \
        .gitignore \
        src/test/java/com/example/kreconomonmon/config/AppPropertiesTest.java
git commit -m "feat: add Spring profile config and environment variable setup"
```

---

## Task 3: Thymeleaf 공통 레이아웃 구성 (P1-3)

**Files:**
- Create: `src/main/resources/templates/layout/default.html`
- Create: `src/main/resources/templates/fragments/header.html`
- Create: `src/main/resources/templates/fragments/footer.html`
- Create: `src/main/resources/static/css/custom.css`
- Create: `src/main/resources/static/js/common.js`

**Step 1: 공통 레이아웃 `layout/default.html` 생성**

Thymeleaf Layout Dialect는 `layout:decorate` 방식을 사용한다.
레이아웃 파일이 뼈대이고, 각 페이지가 `layout:decorate`로 레이아웃을 선택한다.

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title layout:title-pattern="$CONTENT_TITLE - 한국 경제 대시보드">한국 경제 대시보드</title>

    <!-- Bootstrap 5.3.3 CSS -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">

    <!-- Custom CSS -->
    <link rel="stylesheet" th:href="@{/css/custom.css}">

    <!-- 페이지별 추가 head 영역 -->
    <th:block layout:fragment="head"></th:block>
</head>
<body>
    <!-- 헤더 -->
    <th:block th:replace="~{fragments/header :: header}"></th:block>

    <!-- 메인 콘텐츠 영역 -->
    <main class="container-fluid py-3">
        <layout:fragment name="content">
            <!-- 각 페이지의 콘텐츠가 여기에 삽입됨 -->
        </layout:fragment>
    </main>

    <!-- 푸터 -->
    <th:block th:replace="~{fragments/footer :: footer}"></th:block>

    <!-- Bootstrap 5.3.3 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmA69wdRhfmAFH3B8MiKP1f4QFY"
            crossorigin="anonymous"></script>

    <!-- Chart.js 4.4.3 -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>

    <!-- Common JS -->
    <script th:src="@{/js/common.js}"></script>

    <!-- 페이지별 추가 스크립트 영역 -->
    <th:block layout:fragment="scripts"></th:block>
</body>
</html>
```

**Step 2: `fragments/header.html` 생성**

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org">
<body>
<header th:fragment="header" class="navbar navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand fw-bold" th:href="@{/}">
            한국 경제 &amp; 서울 부동산 현황
        </a>
        <span class="navbar-text text-white-50 small"
              id="lastUpdated">최종 업데이트: 로딩 중...</span>
    </div>
</header>
</body>
</html>
```

**Step 3: `fragments/footer.html` 생성**

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org">
<body>
<footer th:fragment="footer" class="bg-dark text-white-50 text-center py-3 mt-4">
    <div class="container">
        <small>
            데이터 출처: 한국은행 ECOS API, PostgreSQL aptprice_data DB
            &nbsp;|&nbsp;
            AI 분석: Google Gemini API
        </small>
    </div>
</footer>
</body>
</html>
```

**Step 4: `static/css/custom.css` 생성**

```css
/* 탭 콘텐츠 최소 높이 */
.tab-content .tab-pane {
    min-height: 400px;
}

/* 차트 placeholder 스피너 */
.chart-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 300px;
    background-color: #f8f9fa;
    border-radius: 0.375rem;
}

/* 섹션 카드 */
.section-card {
    margin-bottom: 1.5rem;
}

/* Gemini 분석 영역 */
.analysis-section {
    background-color: #f8f9fa;
    border-left: 4px solid #0d6efd;
    padding: 1rem 1.5rem;
    margin-top: 2rem;
    border-radius: 0 0.375rem 0.375rem 0;
}

/* 에러 메시지 */
.chart-error {
    color: #dc3545;
    text-align: center;
    padding: 2rem;
}
```

**Step 5: `static/js/common.js` 생성**

```javascript
/**
 * 공통 유틸리티 함수
 * Chart.js AJAX 로딩 패턴 공통화
 */

const KrEconoMon = {

    /**
     * 로딩 스피너 HTML 반환
     */
    loadingSpinner: function () {
        return `<div class="chart-placeholder">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                        <div class="mt-2 text-muted small">데이터 로딩 중...</div>
                    </div>
                </div>`;
    },

    /**
     * 에러 메시지 HTML 반환
     */
    errorMessage: function (msg) {
        return `<div class="chart-error">
                    <i class="text-danger">&#9888;</i>
                    <span>${msg || '데이터를 불러올 수 없습니다.'}</span>
                </div>`;
    },

    /**
     * AJAX GET 요청 후 콜백 실행
     * @param {string} url - API 엔드포인트
     * @param {Function} onSuccess - 성공 콜백(data)
     * @param {Function} onError - 실패 콜백(error)
     */
    fetchData: function (url, onSuccess, onError) {
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then(data => onSuccess(data))
            .catch(error => {
                console.error('API 호출 실패:', url, error);
                if (onError) onError(error);
            });
    }
};
```

**Step 6: 레이아웃 파일 구조 확인**

```bash
find C:/Git/kr-economy-mon/src/main/resources/templates -type f
find C:/Git/kr-economy-mon/src/main/resources/static -type f
```

기대 출력:
```
templates/layout/default.html
templates/fragments/header.html
templates/fragments/footer.html
static/css/custom.css
static/js/common.js
```

**Step 7: 커밋**

```bash
git add src/main/resources/templates/ src/main/resources/static/
git commit -m "feat: add Thymeleaf layout with Bootstrap 5.3.3 and Chart.js 4.4.3 CDN"
```

---

## Task 4: MainController 및 메인 페이지 라우팅 (P1-6 일부)

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/controller/MainController.java`
- Create: `src/main/resources/templates/index.html`
- Create: `src/test/java/com/example/kreconomonmon/controller/MainControllerTest.java`

**Step 1: `MainControllerTest.java` 작성 (TDD - 테스트 먼저)**

```java
package com.example.kreconomonmon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexPageShouldReturn200() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"));
    }

    @Test
    void indexPageShouldContainTabElements() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("한국 경제 현황")))
               .andExpect(content().string(org.hamcrest.Matchers.containsString("서울 부동산 현황")));
    }
}
```

**Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.controller.MainControllerTest"
```

기대 출력: `FAILED` (MainController 미구현)

**Step 3: `MainController.java` 구현**

```java
package com.example.kreconomonmon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
```

**Step 4: `templates/index.html` 생성**

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title>대시보드</title>
</head>
<body>

<div layout:fragment="content">
    <!-- 탭 네비게이션 -->
    <ul class="nav nav-tabs mb-3" id="mainTabs" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link active" id="economy-tab"
                    data-bs-toggle="tab" data-bs-target="#economy"
                    type="button" role="tab"
                    aria-controls="economy" aria-selected="true">
                한국 경제 현황
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="realestate-tab"
                    data-bs-toggle="tab" data-bs-target="#realestate"
                    type="button" role="tab"
                    aria-controls="realestate" aria-selected="false">
                서울 부동산 현황
            </button>
        </li>
    </ul>

    <!-- 탭 콘텐츠 -->
    <div class="tab-content" id="mainTabContent">
        <!-- 탭1: 한국 경제 현황 -->
        <div class="tab-pane fade show active" id="economy"
             role="tabpanel" aria-labelledby="economy-tab">
            <th:block th:replace="~{fragments/economy-tab :: economy-tab}"></th:block>
        </div>

        <!-- 탭2: 서울 부동산 현황 -->
        <div class="tab-pane fade" id="realestate"
             role="tabpanel" aria-labelledby="realestate-tab">
            <th:block th:replace="~{fragments/realestate-tab :: realestate-tab}"></th:block>
        </div>
    </div>
</div>

</body>
</html>
```

**Step 5: 테스트 재실행 - 통과 확인**

```bash
./gradlew test --tests "com.example.kreconomonmon.controller.MainControllerTest"
```

기대 출력: `BUILD SUCCESSFUL`, 2 tests passed

**Step 6: 커밋**

```bash
git add src/main/java/com/example/kreconomonmon/controller/MainController.java \
        src/main/resources/templates/index.html \
        src/test/java/com/example/kreconomonmon/controller/MainControllerTest.java
git commit -m "feat: add MainController with index page routing"
```

---

## Task 5: 탭1 경제 현황 목업 레이아웃 (P1-4)

**Files:**
- Create: `src/main/resources/templates/fragments/economy-tab.html`

**Step 1: `fragments/economy-tab.html` 생성**

8개 섹션을 2열 그리드로 배치한다. 각 섹션에는 차트 placeholder와 로딩 스피너를 포함한다.

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org">
<body>
<div th:fragment="economy-tab">

    <div class="row g-3">

        <!-- 섹션1: 금리 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">금리</h6>
                    <small class="text-muted">기준금리 · 국고채 3년 · 국고채 10년</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-interest-rate" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션2: GDP/성장률 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">국민소득 / 성장률</h6>
                    <small class="text-muted">GDP 성장률 · 1인당 GDP</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-gdp" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션3: 환율 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">환율</h6>
                    <small class="text-muted">USD · EUR · JPY(100엔) · CNY</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-exchange-rate" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션4: 물가 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">물가</h6>
                    <small class="text-muted">CPI · PPI</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-price-index" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션5: 무역/경상수지 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">무역 / 경상수지</h6>
                    <small class="text-muted">경상수지 · 수출 · 수입</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-trade" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션6: 고용/경기 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">고용 / 경기</h6>
                    <small class="text-muted">실업률 · 취업자수</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-employment" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션7: 통화/유동성 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">통화 / 유동성</h6>
                    <small class="text-muted">M2 · 외환보유액 (한국·일본·중국)</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-liquidity" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 섹션8: 인구/출산율/고령화 -->
        <div class="col-12 col-lg-6 section-card">
            <div class="card h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold">인구 / 출산율 / 고령화</h6>
                    <small class="text-muted">추계인구 · 고령인구비율 · 합계출산율</small>
                </div>
                <div class="card-body p-2">
                    <div id="chart-population" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div><!-- .row -->

    <!-- Gemini 경제 분석 영역 placeholder -->
    <div class="analysis-section mt-4" id="economy-analysis">
        <h6 class="fw-bold mb-2">AI 경제 분석</h6>
        <div id="economy-analysis-text" class="text-muted">
            <div class="spinner-border spinner-border-sm text-primary me-2" role="status">
                <span class="visually-hidden">분석 중...</span>
            </div>
            경제 데이터 로딩 후 AI 분석이 표시됩니다.
        </div>
    </div>

</div><!-- th:fragment -->
</body>
</html>
```

**Step 2: 탭1 fragment 렌더링 확인**

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8086/krEconoMon` 접속 후 탭1에서 8개 섹션 카드와 스피너가 표시되는지 확인한다.

> 주의: DB 연결이 없으면 앱 시작 시 DataSource 오류가 발생할 수 있다.
> Task 2에서 작성한 `application-local.yml`의 DB 설정이 실제 DB 없이도 구동되도록
> `spring.autoconfigure.exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`을
> 테스트/목업 단계에서 임시로 추가할 수 있다. 또는 환경변수를 설정한다.

**Step 3: 커밋**

```bash
git add src/main/resources/templates/fragments/economy-tab.html
git commit -m "feat: add Tab1 economy section mockup layout with 8 chart placeholders"
```

---

## Task 6: 탭2 부동산 현황 목업 레이아웃 (P1-5)

**Files:**
- Create: `src/main/resources/templates/fragments/realestate-tab.html`

**Step 1: `fragments/realestate-tab.html` 생성**

권역 구성:
- 강남권: 강남구(11680), 서초구(11650), 송파구(11710)
- 강동권: 강동구(11740), 노원구(11350), 성동구(11200)
- 강서권: 강서구(11500), 영등포구(11560), 양천구(11470)
- 강북권: 종로구(11110), 마포구(11440), 용산구(11170)

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org">
<body>
<div th:fragment="realestate-tab">

    <!-- 최상단: KB 지수 차트 -->
    <div class="card mb-4">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h6 class="mb-0 fw-bold">서울 아파트 KB 지수</h6>
            <small class="text-muted">매매지수 · 전세지수 (최근 10년)</small>
        </div>
        <div class="card-body p-2">
            <div id="chart-kb-index" class="chart-placeholder">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">로딩 중...</span>
                </div>
            </div>
        </div>
    </div>

    <!-- 면적 타입 선택 -->
    <div class="d-flex align-items-center mb-3">
        <label for="areaTypeSelect" class="form-label me-2 mb-0 fw-semibold">면적 타입:</label>
        <select id="areaTypeSelect" class="form-select form-select-sm w-auto">
            <option value="UA04" selected>UA04 - 전용 84㎡</option>
            <option value="UA02">UA02 - 전용 59㎡</option>
        </select>
    </div>

    <!-- 4개 권역 탭 -->
    <ul class="nav nav-pills mb-3" id="regionTabs" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link active" id="gangnam-tab"
                    data-bs-toggle="pill" data-bs-target="#gangnam"
                    type="button" role="tab"
                    aria-controls="gangnam" aria-selected="true"
                    data-region="gangnam"
                    data-codes="11680,11650,11710"
                    data-names="강남구,서초구,송파구">
                강남권
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="gangdong-tab"
                    data-bs-toggle="pill" data-bs-target="#gangdong"
                    type="button" role="tab"
                    aria-controls="gangdong" aria-selected="false"
                    data-region="gangdong"
                    data-codes="11740,11350,11200"
                    data-names="강동구,노원구,성동구">
                강동권
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="gangseo-tab"
                    data-bs-toggle="pill" data-bs-target="#gangseo"
                    type="button" role="tab"
                    aria-controls="gangseo" aria-selected="false"
                    data-region="gangseo"
                    data-codes="11500,11560,11470"
                    data-names="강서구,영등포구,양천구">
                강서권
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="gangbuk-tab"
                    data-bs-toggle="pill" data-bs-target="#gangbuk"
                    type="button" role="tab"
                    aria-controls="gangbuk" aria-selected="false"
                    data-region="gangbuk"
                    data-codes="11110,11440,11170"
                    data-names="종로구,마포구,용산구">
                강북권
            </button>
        </li>
    </ul>

    <!-- 권역별 콘텐츠 -->
    <div class="tab-content" id="regionTabContent">

        <!-- 강남권 -->
        <div class="tab-pane fade show active" id="gangnam"
             role="tabpanel" aria-labelledby="gangnam-tab">
            <th:block th:replace="~{fragments/region-content :: region-content('gangnam', '강남권', '강남구,서초구,송파구')}"></th:block>
        </div>

        <!-- 강동권 -->
        <div class="tab-pane fade" id="gangdong"
             role="tabpanel" aria-labelledby="gangdong-tab">
            <th:block th:replace="~{fragments/region-content :: region-content('gangdong', '강동권', '강동구,노원구,성동구')}"></th:block>
        </div>

        <!-- 강서권 -->
        <div class="tab-pane fade" id="gangseo"
             role="tabpanel" aria-labelledby="gangseo-tab">
            <th:block th:replace="~{fragments/region-content :: region-content('gangseo', '강서권', '강서구,영등포구,양천구')}"></th:block>
        </div>

        <!-- 강북권 -->
        <div class="tab-pane fade" id="gangbuk"
             role="tabpanel" aria-labelledby="gangbuk-tab">
            <th:block th:replace="~{fragments/region-content :: region-content('gangbuk', '강북권', '종로구,마포구,용산구')}"></th:block>
        </div>

    </div>

    <!-- Gemini 부동산 분석 영역 placeholder -->
    <div class="analysis-section mt-4" id="realestate-analysis">
        <h6 class="fw-bold mb-2">AI 부동산 분석</h6>
        <div id="realestate-analysis-text" class="text-muted">
            <div class="spinner-border spinner-border-sm text-primary me-2" role="status">
                <span class="visually-hidden">분석 중...</span>
            </div>
            부동산 데이터 로딩 후 AI 분석이 표시됩니다.
        </div>
    </div>

</div><!-- th:fragment -->
</body>
</html>
```

**Step 2: 권역 콘텐츠 공통 fragment `fragments/region-content.html` 생성**

```html
<!DOCTYPE html>
<html lang="ko"
      xmlns:th="http://www.thymeleaf.org">
<body>
<div th:fragment="region-content(regionId, regionName, districtNames)">

    <div class="row g-3">
        <!-- 매매가 차트 -->
        <div class="col-12 col-lg-6">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0 fw-bold">
                        <span th:text="${regionName}">권역</span> 매매가 추이
                    </h6>
                    <small class="text-muted" th:text="${districtNames}">구 목록</small>
                </div>
                <div class="card-body p-2">
                    <div th:id="'chart-trade-' + ${regionId}" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 전세가 차트 -->
        <div class="col-12 col-lg-6">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0 fw-bold">
                        <span th:text="${regionName}">권역</span> 전세가 추이
                    </h6>
                    <small class="text-muted" th:text="${districtNames}">구 목록</small>
                </div>
                <div class="card-body p-2">
                    <div th:id="'chart-lease-' + ${regionId}" class="chart-placeholder">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- TOP5 테이블 -->
    <div class="row g-3 mt-1">
        <!-- 매매 TOP5 -->
        <div class="col-12 col-lg-6">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0 fw-bold">올해 매매가 TOP 5</h6>
                </div>
                <div class="card-body p-2">
                    <div th:id="'table-top5-trade-' + ${regionId}" class="text-muted text-center py-3">
                        <div class="spinner-border spinner-border-sm text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                        데이터 로딩 중...
                    </div>
                </div>
            </div>
        </div>

        <!-- 전세 TOP5 -->
        <div class="col-12 col-lg-6">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0 fw-bold">올해 전세가 TOP 5</h6>
                </div>
                <div class="card-body p-2">
                    <div th:id="'table-top5-lease-' + ${regionId}" class="text-muted text-center py-3">
                        <div class="spinner-border spinner-border-sm text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                        데이터 로딩 중...
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>
</body>
</html>
```

**Step 3: 커밋**

```bash
git add src/main/resources/templates/fragments/realestate-tab.html \
        src/main/resources/templates/fragments/region-content.html
git commit -m "feat: add Tab2 real-estate section mockup layout with region tabs and TOP5 placeholders"
```

---

## Task 7: REST API Controller 기본 구조 (P1-6)

**Files:**
- Create: `src/main/java/com/example/kreconomonmon/controller/EconomyApiController.java`
- Create: `src/main/java/com/example/kreconomonmon/controller/RealEstateApiController.java`
- Create: `src/main/java/com/example/kreconomonmon/controller/GeminiApiController.java`
- Create: `src/main/java/com/example/kreconomonmon/dto/ApiErrorResponse.java`
- Create: `src/test/java/com/example/kreconomonmon/controller/EconomyApiControllerTest.java`

**Step 1: `ApiErrorResponse.java` DTO 생성**

```java
package com.example.kreconomonmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private final String error;
    private final String message;
}
```

**Step 2: `EconomyApiControllerTest.java` 작성 (TDD)**

```java
package com.example.kreconomonmon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EconomyApiController.class)
class EconomyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void interestRateEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/economy/interest-rate"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void gdpEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/economy/gdp"))
               .andExpect(status().isOk());
    }
}
```

**Step 3: `EconomyApiController.java` 구현 (목업 응답)**

```java
package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/economy")
public class EconomyApiController {

    @GetMapping("/interest-rate")
    public ResponseEntity<Map<String, Object>> getInterestRate() {
        // Phase 2에서 ECOS API 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "message", "ECOS API 연동 예정 (Phase 2)"
        ));
    }

    @GetMapping("/gdp")
    public ResponseEntity<Map<String, Object>> getGdp() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/price-index")
    public ResponseEntity<Map<String, Object>> getPriceIndex() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/trade")
    public ResponseEntity<Map<String, Object>> getTrade() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/employment")
    public ResponseEntity<Map<String, Object>> getEmployment() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/liquidity")
    public ResponseEntity<Map<String, Object>> getLiquidity() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/population")
    public ResponseEntity<Map<String, Object>> getPopulation() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }
}
```

**Step 4: `RealEstateApiController.java` 구현 (목업 응답)**

```java
package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/real-estate")
public class RealEstateApiController {

    @GetMapping("/kb-index")
    public ResponseEntity<Map<String, Object>> getKbIndex() {
        // Phase 3에서 ECOS API + DB 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "message", "KB 지수 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "region", region,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/lease")
    public ResponseEntity<Map<String, Object>> getLease(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "region", region,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/top5/trade")
    public ResponseEntity<Map<String, Object>> getTop5Trade(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "sigunguCode", sigunguCode,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/top5/lease")
    public ResponseEntity<Map<String, Object>> getTop5Lease(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "sigunguCode", sigunguCode,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }
}
```

**Step 5: `GeminiApiController.java` 구현 (목업 응답)**

```java
package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiApiController {

    @GetMapping("/economy-analysis")
    public ResponseEntity<Map<String, Object>> getEconomyAnalysis() {
        // Phase 4에서 Gemini API 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "text", "Gemini AI 경제 분석 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/realestate-analysis")
    public ResponseEntity<Map<String, Object>> getRealEstateAnalysis() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "text", "Gemini AI 부동산 분석 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/economy-cartoon")
    public ResponseEntity<Map<String, Object>> getEconomyCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/realestate-cartoon")
    public ResponseEntity<Map<String, Object>> getRealEstateCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Phase 4)"
        ));
    }
}
```

**Step 6: 테스트 실행**

```bash
./gradlew test --tests "com.example.kreconomonmon.controller.EconomyApiControllerTest"
```

기대 출력: `BUILD SUCCESSFUL`, 2 tests passed

**Step 7: 커밋**

```bash
git add src/main/java/com/example/kreconomonmon/controller/ \
        src/main/java/com/example/kreconomonmon/dto/ApiErrorResponse.java \
        src/test/java/com/example/kreconomonmon/controller/EconomyApiControllerTest.java
git commit -m "feat: add REST API controller stubs for economy, real-estate, and Gemini endpoints"
```

---

## Task 8: DB 연결 없이 앱 구동 설정 (목업 단계 임시)

Phase 1 목업 단계에서는 PostgreSQL DB에 연결하지 않아도 앱이 구동될 수 있도록 설정한다.
Phase 3 착수 전까지 DataSource 자동 설정을 제외한다.

**Files:**
- Modify: `src/main/resources/application-local.yml`

**Step 1: `application-local.yml`에 DataSource 자동 설정 제외 추가**

기존 `application-local.yml` 상단에 다음을 추가한다:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

> 이 설정은 Phase 3 DB 연동 작업 시작 전에 제거한다.
> Phase 3 착수 시 이 exclude 블록을 삭제하고 DB 연결 설정을 활성화한다.

**Step 2: `./gradlew bootRun` 실행 및 접속 확인**

```bash
./gradlew bootRun
```

기대 출력:
```
Started KrEconoMonApplication in X.XXX seconds
```

브라우저에서 `http://localhost:8086/krEconoMon` 접속 확인.

**Step 3: 전체 테스트 실행**

```bash
./gradlew test
```

기대 출력: `BUILD SUCCESSFUL`, 모든 테스트 통과

**Step 4: 커밋**

```bash
git add src/main/resources/application-local.yml
git commit -m "chore: exclude DataSource auto-config in local profile for Phase 1 mockup"
```

---

## Task 9: Playwright MCP 검증 (Sprint 1 DoD 확인)

`./gradlew bootRun` 실행 후 아래 순서로 검증한다.

**검증 1: 메인 페이지 접속**

```
browser_navigate -> http://localhost:8086/krEconoMon
browser_snapshot -> 탭1/탭2 탭 버튼 존재 확인, 콘솔 에러 없음
browser_console_messages(level: "error") -> 에러 없음 확인
```

**검증 2: 탭 전환**

```
browser_click -> "서울 부동산 현황" 탭 클릭
browser_snapshot -> 탭2 콘텐츠 활성화 확인 (KB지수 placeholder, 4개 권역 탭 존재)
browser_click -> "한국 경제 현황" 탭 클릭
browser_snapshot -> 탭1 콘텐츠 활성화 확인 (8개 섹션 카드 존재)
```

**검증 3: CDN 리소스 로드**

```
browser_network_requests -> Bootstrap CDN, Chart.js CDN 200 응답 확인
```

**검증 4: REST API 엔드포인트**

```
browser_navigate -> http://localhost:8086/krEconoMon/api/economy/interest-rate
browser_snapshot -> JSON 응답 {"status":"mock",...} 확인
```

**검증 5: 반응형 레이아웃**

```
browser_resize -> 375x812 (모바일)
browser_snapshot -> 모바일 레이아웃 정상 렌더링 확인
browser_resize -> 1920x1080 (데스크톱)
browser_snapshot -> 데스크톱 레이아웃 확인
```

---

## 완료 기준 (Definition of Done)

| 항목 | 확인 방법 | 상태 |
|------|-----------|------|
| `./gradlew build` 성공 | `BUILD SUCCESSFUL` 출력 | ⬜ |
| `./gradlew bootRun` 후 `http://localhost:8086/krEconoMon` 접근 가능 | 브라우저 접속 | ⬜ |
| 탭1/탭2 전환 동작 | Bootstrap 탭 클릭 시 콘텐츠 전환 | ⬜ |
| 탭1 8개 섹션 카드 표시 | 각 섹션 placeholder 렌더링 | ⬜ |
| 탭2 KB지수 + 4개 권역 탭 표시 | 권역 탭 클릭 가능 | ⬜ |
| Bootstrap 5.3.3 CDN 로드 200 | Network 탭 확인 | ⬜ |
| Chart.js 4.4.3 CDN 로드 200 | Network 탭 확인 | ⬜ |
| Thymeleaf Layout Dialect 적용 | header/footer 공통 렌더링 | ⬜ |
| REST API 엔드포인트 8개 (경제) 200 응답 | curl 또는 브라우저 | ⬜ |
| REST API 엔드포인트 5개 (부동산) 200 응답 | curl 또는 브라우저 | ⬜ |
| 환경변수 설정 문서화 (`application-local.yml`) | 파일 존재 및 `${ENV_VAR}` 참조 | ⬜ |
| 전체 테스트 통과 | `./gradlew test` BUILD SUCCESSFUL | ⬜ |

---

## 예상 산출물

Sprint 1 완료 시 다음 파일들이 생성된다:

```
kr-economy-mon/
├── build.gradle
├── settings.gradle
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/com/example/kreconomonmon/
│   │   │   ├── KrEconoMonApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── MainController.java
│   │   │   │   ├── EconomyApiController.java
│   │   │   │   ├── RealEstateApiController.java
│   │   │   │   └── GeminiApiController.java
│   │   │   └── dto/
│   │   │       └── ApiErrorResponse.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── templates/
│   │       │   ├── index.html
│   │       │   ├── layout/
│   │       │   │   └── default.html
│   │       │   └── fragments/
│   │       │       ├── header.html
│   │       │       ├── footer.html
│   │       │       ├── economy-tab.html
│   │       │       ├── realestate-tab.html
│   │       │       └── region-content.html
│   │       └── static/
│   │           ├── css/custom.css
│   │           └── js/common.js
│   └── test/
│       └── java/com/example/kreconomonmon/
│           ├── config/AppPropertiesTest.java
│           └── controller/
│               ├── MainControllerTest.java
│               └── EconomyApiControllerTest.java
└── docs/
    ├── ROADMAP.md
    ├── PRD.md
    └── sprint/
        └── sprint-01.md  (이 파일)
```

---

## 의존성 및 리스크

| 리스크 | 대응 방안 |
|--------|-----------|
| DB 연결 없이 Spring Boot 구동 실패 | Task 8: DataSource AutoConfig exclude 설정으로 해결 |
| Thymeleaf Layout Dialect 버전 충돌 | `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0` 명시적 버전 지정 |
| Gradle Wrapper 다운로드 실패 | 사내 네트워크 프록시 설정 또는 로컬 Gradle 설치 사용 |
| CDN 접근 불가 (폐쇄망 환경) | Bootstrap/Chart.js jar를 `src/main/resources/static/`에 로컬 복사 |
| ECOS/Gemini API Key 미발급 | Phase 1에서는 목업 응답으로 대체하므로 Key 없이 진행 가능 |

---

## 다음 Sprint 연계

Sprint 2 (Phase 2, Week 3~4)에서 진행할 작업:
- ECOS API 클라이언트 서비스 구현 (`EcosApiService`)
- 경제지표 JPA Entity 및 Repository 구현
- 탭1 섹션 1~4 차트 실제 데이터 연동 (금리, GDP, 환율, 물가)
- Task 8의 DataSource exclude 설정 제거 및 DB 연동 활성화

Sprint 1 완료 후 `docs/ROADMAP.md`의 Phase 1 작업 목록을 `[x]`로 업데이트한다.
