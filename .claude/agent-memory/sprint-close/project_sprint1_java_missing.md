---
name: Java 미설치 환경 이슈
description: 이 PC에 Java 21이 설치되어 있지 않아 Gradle 빌드 및 테스트 실행 불가. Sprint 3 착수 전 Java 설치 필수.
type: project
---

Java 21 (build.gradle toolchain 21 설정)이 이 PC에 설치되어 있지 않아 `./gradlew build`, `./gradlew test`, `./gradlew bootRun` 실행이 불가능하다. Sprint 2에서도 동일한 이유로 정적 코드 검토만 수행.

**Why:** Sprint 1, Sprint 2 마무리 시 코드 정적 검토만 수행하였고 실제 빌드/실행 검증은 진행하지 못함. build.gradle이 Java 21 toolchain으로 업데이트됨.

**How to apply:** Sprint 3 착수 전 반드시 Java 21 LTS(Temurin 21) 설치 확인 후 `./gradlew test`로 Sprint 2 테스트 8개 통과 확인, `./gradlew bootRun`으로 서버 구동 검증을 수행할 것.
