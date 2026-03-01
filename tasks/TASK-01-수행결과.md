# TASK-01 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-sonnet-4-6)
> 상태: **완료**

---

## 1. 작업 개요

5개 도메인 테이블(patients, devices, patient_device_assignments, collection_item_definitions, patient_biometric_history)의
JPA Entity·Repository·SQL 스크립트를 생성하고, TimescaleDB 하이퍼테이블과 수집 항목 마스터 14건을 초기화했다.

---

## 2. 완료 기준 달성 현황

| # | 완료 기준 | 결과 |
|---|---|---|
| 1 | 5개 테이블 DDL 오류 없이 실행 | ✅ |
| 2 | TimescaleDB 하이퍼테이블 생성 확인 | ✅ |
| 3 | 인덱스 3종 생성 확인 | ✅ |
| 4 | 수집 항목 14건 INSERT 완료 | ✅ |
| 5 | QueryDSL Q클래스 정상 생성 | ✅ |
| 6 | Entity 단위 테스트 통과 (저장·조회·관계) | ✅ (11건 전원 통과) |

---

## 3. 체크리스트 완료 현황

### 1-1. Enum 클래스 (5종)

| 항목 | 결과 |
|---|---|
| `PatientStatus` — ACTIVE, INACTIVE, COMPLETED, DELETED | ✅ |
| `DeviceStatus` — AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED | ✅ |
| `AssignmentStatus` — ACTIVE, RETURNED, LOST | ✅ |
| `ItemCategory` — VITAL_SIGN, ACTIVITY, SLEEP, AI_SCORE | ✅ |
| `CollectionMode` — CONTINUOUS, INTERVAL, SLEEP_ONLY | ✅ |

### 1-2. Entity 클래스 (5종)

| 항목 | 결과 |
|---|---|
| `Patient.java` — patient_code UNIQUE, PatientStatus, JPA Auditing | ✅ |
| `Device.java` — serial_number UNIQUE, DeviceStatus, battery_level, last_sync_at | ✅ |
| `PatientDeviceAssignment.java` — @ManyToOne 지연 로딩, AssignmentStatus | ✅ |
| `CollectionItemDefinition.java` — item_code UNIQUE, ItemCategory, CollectionMode, hc_record_type | ✅ |
| `PatientBiometricHistory.java` — measured_at 파티션 키, value_numeric/text/json, @JdbcTypeCode(JSON) | ✅ |

### 1-3. Repository (5종)

| 항목 | 결과 |
|---|---|
| `PatientRepository` — findByPatientCode, existsByPatientCode, findTopBy...CodeDesc | ✅ |
| `DeviceRepository` — findBySerialNumber, existsBySerialNumber, findByDeviceStatus | ✅ |
| `PatientDeviceAssignmentRepository` — findByPatientIdAndStatus, findTop5By... | ✅ |
| `CollectionItemDefinitionRepository` — findByIsActiveTrueOrderByDisplayOrder, findByItemCode | ✅ |
| `PatientBiometricHistoryRepository` + QueryDSL Custom (findByCondition, findDailySummary) | ✅ |

### 1-4. SQL 스크립트 (3종)

| 항목 | 결과 |
|---|---|
| `schema.sql` — 5개 테이블 DDL (DROP IF EXISTS + CREATE TABLE IF NOT EXISTS) | ✅ |
| `timescaledb.sql` — 하이퍼테이블 (월 단위) + 인덱스 3종 + PK (id, measured_at) | ✅ |
| `master_data.sql` — 14건 INSERT (참조용) | ✅ |

### 1-5. 초기화 설정

| 항목 | 결과 |
|---|---|
| `DataInitializer.java` — item_code 중복 확인 후 INSERT, dev 프로파일 테스트 데이터 3건 | ✅ |
| `application-dev.yml` — spring.sql.init (schema.sql + timescaledb.sql) | ✅ |

---

## 4. 발견 이슈 및 수정 내역

### 이슈 #1 — Testcontainers TimescaleDB 이미지 호환성 선언 누락

**증상**: `DockerImageName` 의 `assertCompatibleWith("postgres")` 실패
```
org.testcontainers.utility.DockerImageName$InvalidSubstituteException:
  timescale/timescaledb:latest-pg16 is not a compatible substitute for postgres
```
**원인**: Testcontainers가 non-postgres 이미지를 postgres 컨테이너로 사용할 때 명시적 선언 필요
**수정**: 3개 테스트 클래스 전체
```java
// 수정 전
new PostgreSQLContainer<>(DockerImageName.parse("timescale/timescaledb:latest-pg16"))
// 수정 후
new PostgreSQLContainer<>(
    DockerImageName.parse("timescale/timescaledb:latest-pg16")
                   .asCompatibleSubstituteFor("postgres"))
```

---

### 이슈 #2 — spring-boot-testcontainers 의존성 누락

**증상**: `package org.springframework.boot.testcontainers.service.connection does not exist`
**원인**: `@ServiceConnection` 애너테이션은 `spring-boot-testcontainers` 모듈에 있으나 미포함
**수정**: `backend/build.gradle`
```groovy
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
```

---

### 이슈 #3 — @DataJpaTest에서 JPAQueryFactory 빈 누락

**증상**: `NoSuchBeanDefinitionException: No qualifying bean of type 'JPAQueryFactory'`
**원인**: `@DataJpaTest`는 `QueryDslConfig`를 자동 로드하지 않음
**수정**: 3개 테스트 클래스에 `@Import({QueryDslConfig.class, JpaAuditingConfig.class})` 추가

---

### 이슈 #4 — @DataJpaTest에서 JPA Auditing null 오류

**증상**: `null value in column "created_at" violates not-null constraint`
**원인**: `@DataJpaTest` 슬라이스는 `@EnableJpaAuditing`을 포함한 `JpaAuditingConfig`를 로드하지 않음
**수정**: 이슈 #3과 동일하게 `@Import({QueryDslConfig.class, JpaAuditingConfig.class})` 추가

---

### 이슈 #5 — value_json 컬럼 JSONB 타입 불일치

**증상**: `column "value_json" is of type jsonb but expression is of type character varying`
**원인**: Hibernate 6에서 `String` 필드를 JSONB에 매핑 시 명시적 타입 힌트 필요
**수정**: `PatientBiometricHistory.java`
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "value_json", columnDefinition = "jsonb")
private String valueJson;
```

---

### 이슈 #6 — DailySummaryDto avgValue 타입 불일치

**증상**: `No constructor found for DailySummaryDto with argument types [..., Double, ...]`
**원인**: QueryDSL `avg()` 반환 타입은 `Double`이나 DTO가 `BigDecimal avgValue`로 선언됨
**수정**: `DailySummaryDto.java` — `avgValue` 타입 `BigDecimal` → `Double` 변경

---

### 이슈 #7 — DateTemplate 타입 vs DailySummaryDto 생성자 타입 불일치

**증상**: `argument type mismatch` (findDailySummary 실행 시)
**원인**: `Expressions.dateTemplate(LocalDate.class, "CAST(...) AS DATE", ...)` 결과가
Hibernate 6에서 `java.sql.Date`로 반환되나 DTO 생성자가 `LocalDate`만 수용
**수정**:
- `PatientBiometricHistoryRepositoryImpl.java`: `dateTemplate` 클래스를 `java.sql.Date.class`로 변경
- `DailySummaryDto.java`: `java.sql.Date` 수용 오버로드 생성자 추가 (`.toLocalDate()` 변환)

---

### 이슈 #8 — Windows 네이티브 PostgreSQL 포트 충돌

**증상**: `PSQLException: FATAL: password authentication failed for user 'wearable'`
**원인**: `C:\Program Files\PostgreSQL\18\bin\postgres.exe` (PID 9116)이 포트 5432를 선점하여
Docker 컨테이너 포트 매핑 불가. Spring Boot가 네이티브 PostgreSQL에 연결 시도
**수정**:
- `docker-compose.yml`: `"5432:5432"` → `"5433:5432"`
- `application-dev.yml`: `jdbc:postgresql://localhost:5432/` → `jdbc:postgresql://localhost:5433/`

---

## 5. 최종 검증 결과

### 5-1. 단위 테스트 (./gradlew test)

```
PatientRepositoryTest > 환자 저장 및 patient_code 조회 PASSED
PatientRepositoryTest > existsByPatientCode 확인 PASSED
PatientRepositoryTest > findTopByOrderByPatientCodeDesc 채번 확인 PASSED
PatientRepositoryTest > status 변경 후 조회 PASSED
DeviceRepositoryTest > 기기 저장 및 serialNumber 조회 PASSED
DeviceRepositoryTest > existsBySerialNumber 확인 PASSED
DeviceRepositoryTest > findByDeviceStatus 조회 PASSED
PatientBiometricHistoryRepositoryTest > 생체신호 저장 및 INSERT 확인 PASSED
PatientBiometricHistoryRepositoryTest > QueryDSL — 환자별 조건 조회 (findByCondition) PASSED
PatientBiometricHistoryRepositoryTest > QueryDSL — 일별 요약 조회 (findDailySummary) PASSED
PatientBiometricHistoryRepositoryTest > 할당 상태 변경 관계 확인 PASSED

BUILD SUCCESSFUL in 1m 37s
11 tests: 11 passed
```

### 5-2. 서버 기동 (./gradlew bootRun --args='--spring.profiles.active=dev')

```
Started WearableMonitorApplication in 5.876 seconds (process running for 6.251)
[DataInitializer] 수집 항목 마스터 데이터 초기화 완료
HikariPool-1 - Start completed. (localhost:5433 → wearable_db)
```

### 5-3. 5개 테이블 확인

```
 Schema |            Name             | Type  |  Owner
--------+-----------------------------+-------+----------
 public | collection_item_definitions | table | wearable
 public | devices                     | table | wearable
 public | patient_biometric_history   | table | wearable
 public | patient_device_assignments  | table | wearable
 public | patients                    | table | wearable
(5 rows)
```

### 5-4. TimescaleDB 하이퍼테이블 확인

```
      hypertable_name      | num_chunks
---------------------------+------------
 patient_biometric_history |          0
(1 row)
```

### 5-5. 마스터 데이터 14건 확인

```
   item_code    | item_name_ko  |  category
----------------+---------------+------------
 HR             | 심박수        | VITAL_SIGN
 RESTING_HR     | 안정시 심박수 | VITAL_SIGN
 HRV            | 심박변이도    | VITAL_SIGN
 SPO2           | 산소포화도    | VITAL_SIGN
 RESPIRATORY    | 호흡수        | VITAL_SIGN
 SKIN_TEMP      | 피부 온도     | VITAL_SIGN
 STRESS         | 스트레스 지수 | VITAL_SIGN
 STEPS          | 걸음 수       | ACTIVITY
 CALORIES       | 소모 칼로리   | ACTIVITY
 EXERCISE       | 운동 세션     | ACTIVITY
 SLEEP_DURATION | 수면 시간     | SLEEP
 SLEEP_STAGE    | 수면 단계     | SLEEP
 SLEEP_SCORE    | 수면 점수     | SLEEP
 ENERGY_SCORE   | 에너지 점수   | AI_SCORE
(14 rows)
```

### 5-6. dev 테스트 데이터 확인

```
-- 환자 3명
 patient_code |  name  | status
--------------+--------+--------
 PT-0001      | 홍길동 | ACTIVE
 PT-0002      | 김영희 | ACTIVE
 PT-0003      | 이철수 | ACTIVE

-- 기기 3대
 serial_number |   model_name   | device_status
---------------+----------------+---------------
 SN-WATCH-001  | Galaxy Watch 7 | AVAILABLE
 SN-WATCH-002  | Galaxy Watch 7 | AVAILABLE
 SN-WATCH-003  | Galaxy Watch 7 | AVAILABLE
```

---

## 6. 후속 TASK 유의사항

| 항목 | 내용 |
|---|---|
| PostgreSQL 포트 | Windows 네이티브 PostgreSQL 18이 5432 선점. 개발 환경 DB 포트는 **5433** 사용 |
| Redis 포트 | TASK-00에서 변경된 그대로 **6740** 유지 |
| JPA Auditing 테스트 | `@DataJpaTest` 사용 시 `@Import({QueryDslConfig.class, JpaAuditingConfig.class})` 필수 |
| JSONB 컬럼 | `String` 타입 필드에 `@JdbcTypeCode(SqlTypes.JSON)` 반드시 선언 |
| QueryDSL avg() | 반환 타입이 `Double`임을 주의 (`BigDecimal` 사용 불가) |
| 마스터 데이터 | DataInitializer가 서버 기동 시 자동 삽입 — 별도 SQL 실행 불필요 |

---

## 7. 산출물 목록

### 신규 생성 파일

| 파일 경로 | 용도 |
|---|---|
| `domain/patient/PatientStatus.java` | Enum |
| `domain/device/DeviceStatus.java` | Enum |
| `domain/assignment/AssignmentStatus.java` | Enum |
| `domain/itemdef/ItemCategory.java` | Enum |
| `domain/itemdef/CollectionMode.java` | Enum |
| `domain/patient/Patient.java` | Entity |
| `domain/device/Device.java` | Entity |
| `domain/assignment/PatientDeviceAssignment.java` | Entity |
| `domain/itemdef/CollectionItemDefinition.java` | Entity |
| `domain/biometric/PatientBiometricHistory.java` | Entity |
| `domain/patient/PatientRepository.java` | Repository |
| `domain/device/DeviceRepository.java` | Repository |
| `domain/assignment/PatientDeviceAssignmentRepository.java` | Repository |
| `domain/itemdef/CollectionItemDefinitionRepository.java` | Repository |
| `domain/biometric/PatientBiometricHistoryRepositoryCustom.java` | QueryDSL Custom 인터페이스 |
| `domain/biometric/PatientBiometricHistoryRepository.java` | Repository (Custom 확장) |
| `domain/biometric/PatientBiometricHistoryRepositoryImpl.java` | QueryDSL 구현체 |
| `domain/biometric/DailySummaryDto.java` | QueryDSL 결과 매핑 DTO |
| `resources/sql/schema.sql` | 5개 테이블 DDL |
| `resources/sql/timescaledb.sql` | 하이퍼테이블 + 인덱스 |
| `resources/sql/master_data.sql` | 마스터 데이터 14건 (참조용) |
| `common/DataInitializer.java` | ApplicationRunner |
| `test/.../PatientRepositoryTest.java` | @DataJpaTest |
| `test/.../DeviceRepositoryTest.java` | @DataJpaTest |
| `test/.../PatientBiometricHistoryRepositoryTest.java` | @DataJpaTest |

> 모든 Java 경로 공통 prefix: `backend/src/main/java/com/wearable/monitor/`
> 테스트 경로 prefix: `backend/src/test/java/com/wearable/monitor/`

### 수정 파일

| 파일 경로 | 변경 내용 |
|---|---|
| `backend/build.gradle` | `testImplementation 'org.springframework.boot:spring-boot-testcontainers'` 추가 |
| `backend/src/main/resources/application-dev.yml` | spring.sql.init 설정 추가, DB 포트 5432 → 5433 |
| `docker-compose.yml` | PostgreSQL 포트 매핑 `"5432:5432"` → `"5433:5432"` |
