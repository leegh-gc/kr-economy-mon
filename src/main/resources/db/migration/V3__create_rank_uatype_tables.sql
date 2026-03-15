-- rank_uatype_sigungu 및 rank_uatype_sigungu_lease 테이블은 이미 존재하는 사전 적재 데이터 테이블입니다.
-- 이 마이그레이션은 기존 테이블과의 버전 정합성을 위한 placeholder입니다.

CREATE TABLE IF NOT EXISTS rank_uatype_sigungu (
    id BIGSERIAL PRIMARY KEY,
    sigungu_code VARCHAR(10) NOT NULL,
    use_area_type VARCHAR(10),
    deal_year INTEGER,
    rank_type INTEGER,
    apt_name VARCHAR(100),
    dong_name VARCHAR(50),
    build_year INTEGER,
    avg_price DOUBLE PRECISION,
    min_price DOUBLE PRECISION,
    max_price DOUBLE PRECISION,
    deal_count INTEGER
);

CREATE TABLE IF NOT EXISTS rank_uatype_sigungu_lease (
    id BIGSERIAL PRIMARY KEY,
    sigungu_code VARCHAR(10) NOT NULL,
    use_area_type VARCHAR(10),
    deal_year INTEGER,
    rank_type INTEGER,
    rent_gbn VARCHAR(5),
    apt_name VARCHAR(100),
    dong_name VARCHAR(50),
    build_year INTEGER,
    avg_deposit DOUBLE PRECISION,
    min_deposit DOUBLE PRECISION,
    max_deposit DOUBLE PRECISION,
    deal_count INTEGER
);
