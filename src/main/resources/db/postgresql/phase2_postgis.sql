-- Phase 2 PostGIS 전환용 스키마 확장 SQL입니다.
-- 운영/로컬 PostgreSQL에서 수동 실행하거나 마이그레이션 도구(Flyway 등)에 연결해 사용합니다.

-- PostGIS 확장을 활성화합니다.
CREATE EXTENSION IF NOT EXISTS postgis;

-- sitter_profiles 테이블에 geography(Point, 4326) 컬럼을 추가합니다.
ALTER TABLE sitter_profiles
    ADD COLUMN IF NOT EXISTS location geography(Point, 4326);

-- 기존 latitude/longitude 데이터로 location을 동기화합니다.
UPDATE sitter_profiles
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL;

-- 공간 인덱스(GIST)를 생성합니다.
CREATE INDEX IF NOT EXISTS idx_sitter_profiles_location_gist
    ON sitter_profiles
    USING GIST (location);
