-- Sprint 5: analysis_cache 테이블 image_data 컬럼 추가

ALTER TABLE analysis_cache ADD COLUMN IF NOT EXISTS image_data TEXT;
