---
name: kr-economy-mon project context
description: Korean economy and Seoul real estate dashboard - Spring Boot + Thymeleaf + Chart.js project with ECOS API, PostgreSQL, and Gemini AI
type: project
---

Project: 한국 경제 & 서울 부동산 현황 대시보드 (kr-economy-mon)

**Tech Stack (PRD-defined):**
- Backend: Spring Boot 3.3.2 MVC + Thymeleaf Layout Dialect 3.3.0
- DB: PostgreSQL (leeghhome.iptime.org:5432/aptprice_data) + Spring Data JPA + QueryDSL 5.0.0
- Charts: Chart.js 4.4.3 (CDN, AJAX)
- CSS: Bootstrap 5.3.3 (CDN)
- AI: Gemini REST API (java.net.http.HttpClient)
- External Data: ECOS API (한국은행)
- Build: Gradle 8.8
- Port: 8086, context-path: /krEconoMon

**Note:** User initially mentioned Python/Streamlit stack but PRD specifies Spring Boot/Java stack. Followed PRD as authoritative source.

**Why:** PRD is the single source of truth for technical decisions.

**How to apply:** Always reference PRD tech stack when generating implementation code or architecture decisions for this project.

**Structure:**
- Tab1: 8 economic indicator sections (interest rate, GDP, exchange rate, CPI, trade, employment, liquidity, population)
- Tab2: Seoul real estate (KB index + 4 regions x 3 districts + TOP5 apartments)
- Gemini AI: economic analysis text + cartoon generation with caching

**Roadmap:** 5 Phases, 5 Sprints (2 weeks each), ~10 weeks total. Created 2026-03-14.
