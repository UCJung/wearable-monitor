# TASK-01 — DB 설계 및 초기화

> 선행: TASK-00 | 후속: TASK-02

---

## 작업 목표
5개 테이블 DDL, JPA Entity, Repository를 생성하고
TimescaleDB 하이퍼테이블 및 수집 항목 마스터 14건을 초기화한다.

---

## 체크리스트

### 1-1. Enum 클래스 (5종)
- [ ] `PatientStatus` — ACTIVE, INACTIVE, COMPLETED, DELETED
- [ ] `DeviceStatus` — AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED
- [ ] `AssignmentStatus` — ACTIVE, RETURNED, LOST
- [ ] `ItemCategory` — VITAL_SIGN, ACTIVITY, SLEEP, AI_SCORE
- [ ] `CollectionMode` — CONTINUOUS, INTERVAL, SLEEP_ONLY

### 1-2. Entity 클래스 (5종)
- [ ] `Patient.java`
  - patient_code VARCHAR(20) UNIQUE
  - status: `PatientStatus` @Enumerated(STRING)
  - @CreatedDate, @LastModifiedDate (JPA Auditing)
- [ ] `Device.java`
  - serial_number UNIQUE
  - device_status: `DeviceStatus` @Enumerated(STRING)
  - battery_level, last_sync_at
- [ ] `PatientDeviceAssignment.java`
  - @ManyToOne Patient, Device (지연 로딩)
  - assignment_status: `AssignmentStatus`
  - monitoring_start_date, monitoring_end_date
- [ ] `CollectionItemDefinition.java`
  - item_code UNIQUE
  - category: `ItemCategory`, collection_mode: `CollectionMode`
  - hc_record_type (Health Connect Record 타입명)
- [ ] `PatientBiometricHistory.java`
  - measured_at: 파티션 키 (TimescaleDB)
  - value_numeric DECIMAL(12,4), value_text, value_json
  - @ManyToOne AssignmentId, ItemDefId (INSERT 전용, 조회는 item_code 사용)

### 1-3. Repository (5종)
- [ ] `PatientRepository`
  ```java
  Optional<Patient> findByPatientCode(String code);
  boolean existsByPatientCode(String code);
  Optional<Patient> findTopByOrderByPatientCodeDesc(); // 채번용
  ```
- [ ] `DeviceRepository`
  ```java
  Optional<Device> findBySerialNumber(String serial);
  boolean existsBySerialNumber(String serial);
  List<Device> findByDeviceStatus(DeviceStatus status);
  ```
- [ ] `PatientDeviceAssignmentRepository`
  ```java
  Optional<PatientDeviceAssignment> findByPatientIdAndAssignmentStatus(
      Long patientId, AssignmentStatus status);
  List<PatientDeviceAssignment> findTop5ByPatientIdOrderByAssignedAtDesc(Long id);
  ```
- [ ] `CollectionItemDefinitionRepository`
  ```java
  List<CollectionItemDefinition> findByIsActiveTrueOrderByDisplayOrder();
  Optional<CollectionItemDefinition> findByItemCode(String code);
  ```
- [ ] `PatientBiometricHistoryRepository` + **Custom QueryDSL 구현**
  ```java
  // Custom 인터페이스
  Page<PatientBiometricHistory> findByCondition(
      Long patientId, List<String> itemCodes,
      LocalDateTime start, LocalDateTime end, Pageable pageable);
  List<DailySummaryDto> findDailySummary(
      List<Long> patientIds, LocalDate start, LocalDate end);
  ```

### 1-4. SQL 스크립트 (3종)
- [ ] `src/main/resources/sql/schema.sql` — 테이블 DDL
  ```sql
  -- 생성 순서: patients → devices → patient_device_assignments
  --           → collection_item_definitions → patient_biometric_history
  -- 각 테이블 DROP IF EXISTS + CREATE TABLE IF NOT EXISTS
  ```
- [ ] `src/main/resources/sql/timescaledb.sql`
  ```sql
  -- 하이퍼테이블 생성 (월 단위 파티션)
  -- 인덱스 3종: 조회용 복합, UNIQUE 중복방지, 카테고리별
  ```
- [ ] `src/main/resources/sql/master_data.sql` — 수집 항목 14건 INSERT
  ```sql
  -- 14개 항목 전체:
  -- 생체신호(7): HR, RESTING_HR, HRV, SPO2, RESPIRATORY, SKIN_TEMP, STRESS
  -- 활동(3): STEPS, CALORIES, EXERCISE
  -- 수면(3): SLEEP_DURATION, SLEEP_STAGE, SLEEP_SCORE
  -- AI(1): ENERGY_SCORE
  -- 각 항목에 hc_record_type, unit, collection_interval_desc, display_order 포함
  ```

### 1-5. 초기화 설정
- [ ] `DataInitializer.java` (ApplicationRunner)
  - 마스터 데이터 중복 없이 INSERT (item_code로 존재 확인 후 삽입)
  - 개발 환경 전용 테스트 환자/장치 데이터 3건 (dev 프로파일만)
- [ ] `application-dev.yml` spring.sql.init 설정
  ```yaml
  spring:
    sql:
      init:
        mode: always
        schema-locations: classpath:sql/schema.sql
  ```

---

## 수집 항목 14종 상세 (master_data.sql 기준)

| item_code | item_name_ko | category | hc_record_type | unit | 수집 주기 |
|---|---|---|---|---|---|
| HR | 심박수 | VITAL_SIGN | HeartRateRecord | BPM | 연속 |
| RESTING_HR | 안정시 심박수 | VITAL_SIGN | RestingHeartRateRecord | BPM | 일 1회 |
| HRV | 심박변이도 | VITAL_SIGN | HeartRateVariabilityRmssdRecord | ms | 수면 중 |
| SPO2 | 산소포화도 | VITAL_SIGN | OxygenSaturationRecord | % | 수면 중 |
| RESPIRATORY | 호흡수 | VITAL_SIGN | RespiratoryRateRecord | 회/분 | 수면 중 |
| SKIN_TEMP | 피부 온도 | VITAL_SIGN | SkinTemperatureRecord | °C | 수면 중 |
| STRESS | 스트레스 지수 | VITAL_SIGN | StressRecord | 점 | 30분 |
| STEPS | 걸음 수 | ACTIVITY | StepsRecord | 걸음 | 연속 |
| CALORIES | 소모 칼로리 | ACTIVITY | TotalCaloriesBurnedRecord | kcal | 연속 |
| EXERCISE | 운동 세션 | ACTIVITY | ExerciseSessionRecord | 분 | 자동 감지 |
| SLEEP_DURATION | 수면 시간 | SLEEP | SleepSessionRecord | 시간 | 매일 밤 |
| SLEEP_STAGE | 수면 단계 | SLEEP | SleepStageRecord | - | 수면 중 |
| SLEEP_SCORE | 수면 점수 | SLEEP | SleepSessionRecord | 점 | 매일 아침 |
| ENERGY_SCORE | 에너지 점수 | AI_SCORE | - | 점 | 매일 아침 |

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-01.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-01

### 생성 파일 목록 (경로 | 용도)

### Entity 관계 요약
(Patient ←→ Assignment ←→ Device / Assignment → BiometricHistory)

### TimescaleDB 적용 시 주의사항

### 테스트 방법

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-01 승인.

아래 순서로 작업해줘.

1. Enum 클래스 5종 생성
2. Entity 5종 생성 (JPA Auditing, 관계 설정 포함)
3. Repository 5종 생성
   - BiometricHistory는 QueryDSL Custom 포함
4. SQL 스크립트 3종 작성
   - schema.sql: DROP IF EXISTS + CREATE TABLE IF NOT EXISTS
   - timescaledb.sql: 하이퍼테이블 + 인덱스
   - master_data.sql: 수집 항목 14건 (위 표 기준 전부 포함)
5. DataInitializer.java 작성 (중복 삽입 방지)
6. application-dev.yml 초기화 설정 추가

작업 중 FK 관계나 인덱스 설계에서 불명확한 부분은 질문해줘.
```

### Step 3 — 완료 검증
```
TASK-01 완료 검증을 해줘.

1. `docker-compose up -d` 후 DB 연결 확인
2. `./gradlew bootRun --args='--spring.profiles.active=dev'` 실행
3. schema.sql 5개 테이블 생성 확인 (psql 또는 로그)
4. timescaledb.sql 하이퍼테이블 생성 확인
   `SELECT * FROM timescaledb_information.hypertables;`
5. master_data.sql 14건 INSERT 확인
   `SELECT item_code, item_name_ko FROM collection_item_definitions ORDER BY display_order;`
6. QueryDSL Q클래스 생성 확인 (`./gradlew compileJava`)

오류가 있으면 수정하고 다시 알려줘.
```

---

## 완료 기준
- [ ] 5개 테이블 DDL 오류 없이 실행
- [ ] TimescaleDB 하이퍼테이블 생성 확인
- [ ] 인덱스 3종 생성 확인
- [ ] 수집 항목 14건 INSERT 완료
- [ ] QueryDSL Q클래스 정상 생성
- [ ] Entity 단위 테스트 통과 (저장·조회·관계)
