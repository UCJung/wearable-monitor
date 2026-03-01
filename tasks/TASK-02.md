# TASK-02 — Back-end: 인증 + 환자·장치 관리 API

> 선행: TASK-01 | 후속: TASK-03, TASK-05, TASK-09
> 관련 기능: FNT-001 (환자 관리), FNT-002 (장치 관리)

---

## 작업 목표
JWT 인증 API, 환자 CRUD, 장치 CRUD, 장치 할당·해제 API를 구현한다.
입력값 유효성 검증과 비즈니스 규칙 예외 처리를 포함한다.

---

## 체크리스트

### 2-1. 인증 API
- [ ] `AuthController` — POST /api/v1/auth/login, /refresh, /logout
- [ ] `AuthService`
  - UserDetailsService 구현 (환자·관리자 통합 조회)
  - BCrypt 비밀번호 검증
  - Access Token 1시간 + Refresh Token 7일 발급
  - 로그아웃 시 Redis 블랙리스트 등록
- [ ] `LoginRequestDto` (patientId, password)
- [ ] `TokenResponseDto` (accessToken, refreshToken, role)
- [ ] `JwtAuthenticationFilter` (요청마다 토큰 검증)

### 2-2. 환자 관리 API (FNT-001)
- [ ] `PatientController`
  ```
  GET    /api/v1/patients          목록 (페이징 + 검색)
  GET    /api/v1/patients/{id}     상세 (할당 장치 + 이력 5건)
  POST   /api/v1/patients          등록
  PUT    /api/v1/patients/{id}     수정
  DELETE /api/v1/patients/{id}     소프트 삭제
  ```
- [ ] `PatientService`
  - `getPatients(condition, pageable)` — QueryDSL 다중 조건 검색
  - `getPatientDetail(id)` — 기본정보 + 현재 할당 장치 + 이력 5건 조합
  - `createPatient(req)` — patient_code PT-NNNN 자동 채번
  - `updatePatient(id, req)`
  - `deletePatient(id)` — 장치 할당 중이면 PATIENT_HAS_ACTIVE_DEVICE 예외
- [ ] DTO
  - `CreatePatientRequest` (@Valid: 이름 2~50자, 생년월일 과거, 성별 M/F)
  - `UpdatePatientRequest`
  - `PatientSearchCondition` (name, patientCode, status, hasDevice)
  - `PatientListResponse` (페이징용 요약 정보)
  - `PatientDetailResponse` (상세 + 장치 현황 + 이력 포함)

### 2-3. 장치 관리 API (FNT-002)
- [ ] `DeviceController`
  ```
  GET    /api/v1/devices           목록 (시리얼·상태 검색)
  GET    /api/v1/devices/{id}      상세 (할당 환자 + 배터리 + 동기화)
  POST   /api/v1/devices           등록
  PUT    /api/v1/devices/{id}      수정
  DELETE /api/v1/devices/{id}      소프트 삭제 (RETIRED)
  ```
- [ ] `DeviceService`
  - `createDevice` — serial_number 중복 체크
  - `deleteDevice` — ASSIGNED 상태면 DEVICE_IS_ASSIGNED_CANNOT_DELETE 예외
- [ ] DTO
  - `CreateDeviceRequest` / `UpdateDeviceRequest`
  - `DeviceListResponse` (할당 환자명 포함)
  - `DeviceDetailResponse` (배터리·동기화·이력 포함)

### 2-4. 장치 할당/해제 API
- [ ] `AssignmentController`
  ```
  POST   /api/v1/assignments               장치 할당
  PUT    /api/v1/assignments/{id}/return   장치 해제
  GET    /api/v1/assignments               이력 목록
  ```
- [ ] `AssignmentService`
  - `assignDevice(req)` — 중복 할당 방지 + 장치 상태 ASSIGNED 변경
  - `returnDevice(id, req)` — 장치 상태 AVAILABLE 복원
  - `getAssignments(condition, pageable)`
- [ ] DTO
  - `AssignDeviceRequest` (patientId, deviceId, startDate, endDate, purpose)
  - `ReturnDeviceRequest` (reason)
  - `AssignmentListResponse`

### 2-5. ErrorCode 추가
- [ ] 아래 오류 코드를 `ErrorCode` enum에 추가
  ```java
  PATIENT_NOT_FOUND(404),
  PATIENT_HAS_ACTIVE_DEVICE(400),   // 할당 중 삭제 불가
  DEVICE_NOT_FOUND(404),
  DEVICE_SERIAL_DUPLICATE(400),
  DEVICE_ALREADY_ASSIGNED(400),     // 장치 이미 할당 중
  DEVICE_IS_ASSIGNED_CANNOT_DELETE(400),
  PATIENT_ALREADY_HAS_DEVICE(400),  // 환자 이미 장치 있음
  ASSIGNMENT_NOT_FOUND(404),
  INVALID_TOKEN(401),
  EXPIRED_TOKEN(401),
  ```

### 2-6. 단위 테스트
- [ ] `PatientServiceTest` — 등록·조회·수정·삭제, 비즈니스 예외 케이스
- [ ] `DeviceServiceTest` — 등록·중복·삭제 제한
- [ ] `AssignmentServiceTest` — 할당·해제·중복 할당 예외

---

## 비즈니스 규칙 요약

| 규칙 | 처리 |
|---|---|
| 환자 삭제 시 장치 할당 중 | `PATIENT_HAS_ACTIVE_DEVICE` 400 오류 |
| 장치 삭제 시 ASSIGNED 상태 | `DEVICE_IS_ASSIGNED_CANNOT_DELETE` 400 오류 |
| 이미 장치 있는 환자에게 재할당 | `PATIENT_ALREADY_HAS_DEVICE` 400 오류 |
| ASSIGNED 장치를 다른 환자에게 할당 | `DEVICE_ALREADY_ASSIGNED` 400 오류 |
| patient_code 채번 | DB 최대값 조회 → PT-NNNN 포맷 (동시성: DB SEQUENCE) |

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-02.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-02

### 생성 파일 목록 (패키지 경로 | 역할)

### Service 핵심 비즈니스 로직 요약
(각 메서드별 처리 흐름)

### 트랜잭션 전략
(읽기전용 / 쓰기 구분)

### 테스트 케이스 목록

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-02 승인.

아래 순서로 구현해줘.

1. ErrorCode enum 오류 코드 추가
2. 인증 API (AuthController, AuthService, JwtAuthenticationFilter)
3. 환자 관리
   - DTO 클래스 먼저 생성 (Bean Validation 포함)
   - PatientService (비즈니스 로직 + 예외 처리)
   - PatientController (@Valid, ApiResponse 래퍼)
4. 장치 관리 (같은 순서)
5. 장치 할당·해제 API
6. 단위 테스트 3종

patient_code 채번: DB SEQUENCE (patient_code_seq) 사용해서 동시성 안전하게 구현해줘.
```

### Step 3 — 완료 검증
```
TASK-02 완료 검증을 해줘.

아래 시나리오를 curl 또는 단위 테스트로 확인해줘.

1. POST /api/v1/auth/login → accessToken 발급 확인
2. POST /api/v1/patients → PT-0001 채번 확인
3. POST /api/v1/devices → serial 중복 시 400 확인
4. POST /api/v1/assignments → 환자 상세에 장치 정보 포함 확인
5. DELETE /api/v1/patients/{id} (장치 할당 중) → 400 오류 확인
6. PUT /api/v1/assignments/{id}/return → 장치 AVAILABLE 변경 확인

./gradlew test 결과도 알려줘.
```

---

## 완료 기준
- [ ] 인증 API (로그인·재발급·로그아웃) 동작
- [ ] 환자 CRUD 전체 동작
- [ ] 장치 CRUD 전체 동작
- [ ] 할당·해제 API 동작
- [ ] 비즈니스 예외 처리 전체 확인
- [ ] `./gradlew test` 전체 통과
