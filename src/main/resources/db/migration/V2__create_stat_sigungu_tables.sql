-- stat_sigungu_yymm 및 stat_lease_sigungu 테이블은 이미 존재하는 사전 적재 데이터 테이블입니다.
-- 이 마이그레이션은 기존 테이블과의 버전 정합성을 위한 placeholder입니다.

CREATE TABLE IF NOT EXISTS stat_sigungu_yymm (
    id BIGSERIAL PRIMARY KEY,
    sigungu_code VARCHAR(10) NOT NULL,
    sigungu_name VARCHAR(50),
    use_area_type VARCHAR(10),
    deal_year INTEGER,
    deal_month INTEGER,
    avg_price DOUBLE PRECISION,
    min_price DOUBLE PRECISION,
    max_price DOUBLE PRECISION,
    deal_count INTEGER
);

CREATE TABLE IF NOT EXISTS stat_lease_sigungu (
    id BIGSERIAL PRIMARY KEY,
    sigungu_code VARCHAR(10) NOT NULL,
    sigungu_name VARCHAR(50),
    use_area_type VARCHAR(10),
    deal_year INTEGER,
    deal_month INTEGER,
    rent_gbn VARCHAR(5),
    avg_deposit DOUBLE PRECISION,
    min_deposit DOUBLE PRECISION,
    max_deposit DOUBLE PRECISION,
    deal_count INTEGER
);
