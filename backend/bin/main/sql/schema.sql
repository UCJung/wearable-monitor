-- ============================================================
-- 웨어러블 환자 모니터링 시스템 — 테이블 DDL
-- 실행 순서: patients → devices → patient_device_assignments
--           → collection_item_definitions → patient_biometric_history
-- ============================================================

-- 의존성 역순 DROP
DROP TABLE IF EXISTS patient_biometric_history;
DROP TABLE IF EXISTS collection_item_definitions;
DROP TABLE IF EXISTS patient_device_assignments;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS patient_code_seq;

-- 1. 환자
CREATE TABLE IF NOT EXISTS patients (
    id          BIGSERIAL       NOT NULL,
    patient_code VARCHAR(20)    NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    birth_date  DATE,
    gender      VARCHAR(10),
    notes       TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_patients PRIMARY KEY (id),
    CONSTRAINT uq_patients_code UNIQUE (patient_code)
);

-- 2. 기기
CREATE TABLE IF NOT EXISTS devices (
    id              BIGSERIAL       NOT NULL,
    serial_number   VARCHAR(100)    NOT NULL,
    model_name      VARCHAR(100),
    device_status   VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    battery_level   INT,
    last_sync_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_devices PRIMARY KEY (id),
    CONSTRAINT uq_devices_serial UNIQUE (serial_number)
);

-- 3. 환자-기기 할당
CREATE TABLE IF NOT EXISTS patient_device_assignments (
    id                      BIGSERIAL   NOT NULL,
    patient_id              BIGINT      NOT NULL,
    device_id               BIGINT      NOT NULL,
    assignment_status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    monitoring_start_date   DATE,
    monitoring_end_date     DATE,
    assigned_at             TIMESTAMP   NOT NULL DEFAULT NOW(),
    returned_at             TIMESTAMP,
    CONSTRAINT pk_assignments PRIMARY KEY (id),
    CONSTRAINT fk_assignments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_assignments_device  FOREIGN KEY (device_id)  REFERENCES devices(id)
);

-- 4. 수집 항목 정의 (고정 마스터)
CREATE TABLE IF NOT EXISTS collection_item_definitions (
    id                          BIGSERIAL   NOT NULL,
    item_code                   VARCHAR(50) NOT NULL,
    item_name_ko                VARCHAR(100) NOT NULL,
    category                    VARCHAR(20) NOT NULL,
    collection_mode             VARCHAR(20) NOT NULL,
    hc_record_type              VARCHAR(100),
    unit                        VARCHAR(20),
    collection_interval_desc    VARCHAR(100),
    display_order               INT,
    is_active                   BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_item_definitions PRIMARY KEY (id),
    CONSTRAINT uq_item_code UNIQUE (item_code)
);

-- 5. 생체신호 이력 (TimescaleDB 하이퍼테이블 대상)
-- NOTE: TimescaleDB 2.x는 파티션 키(measured_at)를 포함하지 않는 UNIQUE/PK 제약을 허용하지 않음.
--       ddl-auto: none 환경에서 id 컬럼은 BIGSERIAL로만 선언하고 PRIMARY KEY 제약은 timescaledb.sql에서 처리.
CREATE TABLE IF NOT EXISTS patient_biometric_history (
    id              BIGSERIAL   NOT NULL,
    assignment_id   BIGINT      NOT NULL,
    item_def_id     BIGINT      NOT NULL,
    item_code       VARCHAR(50) NOT NULL,
    measured_at     TIMESTAMP   NOT NULL,
    value_numeric   DECIMAL(12, 4),
    value_text      TEXT,
    value_json      JSONB,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_biometric_assignment FOREIGN KEY (assignment_id) REFERENCES patient_device_assignments(id),
    CONSTRAINT fk_biometric_item_def   FOREIGN KEY (item_def_id)   REFERENCES collection_item_definitions(id)
);

-- 0. patient_code 채번 시퀀스
CREATE SEQUENCE IF NOT EXISTS patient_code_seq START 1;

-- 6. 사용자 (인증용)
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       NOT NULL,
    username    VARCHAR(50)     NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL DEFAULT 'STAFF',
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);
