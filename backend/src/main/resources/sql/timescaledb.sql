-- ============================================================
-- TimescaleDB 하이퍼테이블 설정 및 인덱스
-- schema.sql 실행 후 반드시 실행
-- ============================================================

-- 하이퍼테이블 생성 (월 단위 파티션)
SELECT create_hypertable(
    'patient_biometric_history',
    'measured_at',
    chunk_time_interval => INTERVAL '1 month',
    if_not_exists => TRUE
);

-- 복합 PK (TimescaleDB 하이퍼테이블 — 파티션 키 포함 필수)
ALTER TABLE patient_biometric_history
    ADD CONSTRAINT pk_biometric PRIMARY KEY (id, measured_at);

-- 인덱스 1: 환자별 항목 시계열 조회용 (가장 자주 사용)
CREATE INDEX IF NOT EXISTS idx_biometric_assignment_item_time
    ON patient_biometric_history (assignment_id, item_code, measured_at DESC);

-- 인덱스 2: 중복 방지용 UNIQUE (동일 할당·항목·시각)
CREATE UNIQUE INDEX IF NOT EXISTS idx_biometric_unique_record
    ON patient_biometric_history (assignment_id, item_def_id, measured_at);

-- 인덱스 3: 카테고리(item_code)별 전체 시계열 조회용
CREATE INDEX IF NOT EXISTS idx_biometric_item_time
    ON patient_biometric_history (item_code, measured_at DESC);
