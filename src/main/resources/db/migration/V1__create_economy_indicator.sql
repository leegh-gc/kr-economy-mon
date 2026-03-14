-- 경제지표 캐시 테이블
-- stat_code + item_code + period 조합이 유니크 키
CREATE TABLE IF NOT EXISTS economy_indicator (
    id          BIGSERIAL PRIMARY KEY,
    stat_code   VARCHAR(20)    NOT NULL,   -- ECOS 통계코드 (예: 722Y001)
    item_code   VARCHAR(30)    NOT NULL,   -- 항목코드 (예: 0101000)
    period      VARCHAR(10)    NOT NULL,   -- 기간 (예: 202301, 2023, 2023Q1)
    value       DECIMAL(20, 4),            -- 지표값
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_economy_indicator UNIQUE (stat_code, item_code, period)
);

CREATE INDEX IF NOT EXISTS idx_economy_indicator_stat_item
    ON economy_indicator (stat_code, item_code);
