-- Sprint 4: Gemini 분석 결과 캐시 테이블

CREATE TABLE IF NOT EXISTS analysis_cache (
    cache_key   VARCHAR(50)  NOT NULL PRIMARY KEY,
    cache_type  VARCHAR(30)  NOT NULL,
    content_text TEXT,
    data_hash   VARCHAR(64),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

COMMENT ON TABLE analysis_cache IS 'Gemini AI 분석 결과 캐시 (캐시 키별 최신 분석 텍스트 저장)';
COMMENT ON COLUMN analysis_cache.cache_key IS '캐시 식별자 (예: ECONOMY_ANALYSIS, REALESTATE_ANALYSIS)';
COMMENT ON COLUMN analysis_cache.data_hash IS '분석에 사용된 입력 데이터의 SHA256 해시 (변경 감지용)';
