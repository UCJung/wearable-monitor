# TASK-12 — 역할 기반 로그인 분리 (웹: 의료진 / 앱: 환자)

> 선행: TASK-11 (전체 완료) | 후속: 없음
> 관련 기능: FNT-007 (인증) 확장

---

## 작업 목표
웹은 의료진(STAFF)만 로그인, 안드로이드 앱은 환자(PATIENT)만 로그인할 수 있도록
역할 기반 인증·인가를 구현한다. 환자 등록 시 로그인 계정을 자동 생성한다.

---

## 설계 결정

| 항목 | 결정 |
|---|---|
| 환자 계정 생성 | 환자 등록 시 자동 생성 (ID=환자코드 PT-NNNN, 초기 비밀번호=환자코드+"1!") |
| 환자 API 범위 | 배치 업로드 + 본인 생체신호 이력 조회 + 본인 할당 정보 조회 |
| 역할 검증 방식 | 서버(platform 필드) + 클라이언트 이중 체크 |
| JWT 변경 | role 클레임 추가 → 필터에서 실제 권한 설정 |
| users↔patients 연결 | users 테이블에 patient_id FK 추가 (PATIENT 전용, nullable) |

---

## 체크리스트

### 12-1. DB 스키마 변경
- [x] `schema.sql` — `users` 테이블에 `patient_id BIGINT NULL UNIQUE FK→patients(id)` 추가
- [x] `schema.sql` — DROP 순서 변경 (users를 patients보다 먼저 DROP)

### 12-2. Backend 도메인
- [x] `User.java` — `patientId` 필드 + 생성자 오버로드 + `forPatient()` 팩토리 메서드
- [x] `UserRepository.java` — `findByPatientId()`, `existsByPatientId()` 추가

### 12-3. JWT + Security
- [x] `JwtProvider.java` — JWT payload에 `role` 클레임 추가, `getRole()` 추출 메서드
- [x] `JwtAuthenticationFilter.java` — role 클레임으로 `ROLE_STAFF`/`ROLE_PATIENT` 권한 설정
- [x] `SecurityConfig.java` — `@EnableMethodSecurity` + 역할별 URL 접근 규칙
- [x] `ErrorCode.java` — `STAFF_ONLY_LOGIN`, `PATIENT_ONLY_LOGIN` (FORBIDDEN) 추가

### 12-4. Auth 서비스 로직
- [x] `LoginRequestDto.java` — `platform` 필드 추가 (nullable, "WEB"/"ANDROID")
- [x] `TokenResponseDto.java` — `username` 필드 추가
- [x] `AuthService.java` — platform 기반 역할 검증, JWT에 role 전달, 응답에 username 포함

### 12-5. 환자 계정 자동 생성
- [x] `PatientService.java` — `createPatient()` 내부에서 User(role=PATIENT) 자동 생성
- [x] `DataInitializer.java` — 시드 환자 3명(PT-0001~0003)에 대한 User 계정 생성

### 12-6. 환자 본인 데이터 접근 제한
- [x] `BiometricService` — PATIENT 사용자의 이력 조회 시 본인 patientId 검증
- [x] `AssignmentService` — PATIENT 사용자의 할당 조회 시 본인 환자 검증

### 12-7. Frontend 변경
- [x] `authStore.ts` — `role` 상태 + `setRole()` 액션 추가, persist 포함
- [x] `LoginPage.tsx` — platform: 'WEB' 전송, role 체크, 레이블 수정
- [x] `PrivateRoute.tsx` — `role === 'STAFF'` 검증, 불일치 시 `/login` 리다이렉트
- [x] `authApi.ts` — LoginRequest/LoginResponse 타입 업데이트

### 12-8. Android 변경
- [x] `LoginRequest.kt` — `val platform: String = "ANDROID"` 기본값 필드 추가
- [x] `AuthRepository.kt` — `saveRole()`, `getRole()` 인터페이스 추가
- [x] `AuthRepositoryImpl.kt` — role 저장, PATIENT 체크, clearToken에 KEY_ROLE 포함

### 12-9. 테스트
- [x] 기존 백엔드 테스트 전체 통과 (`./gradlew test`)
- [x] 프론트엔드 빌드 성공 (`npm run build`)
- [x] 안드로이드 빌드 성공 (`./gradlew assembleDebug`)

---

## 완료 기준
- [x] JWT에 role 클레임 포함, 필터에서 권한 설정 동작
- [x] 웹: STAFF만 로그인, PATIENT 시도 시 거부
- [x] 앱: PATIENT만 로그인, STAFF 시도 시 거부
- [x] 환자 등록 시 User 계정 자동 생성 (초기 비밀번호: 환자코드+"1!")
- [x] PATIENT 토큰으로 STAFF 전용 API 접근 시 403
- [x] PATIENT 본인 데이터만 조회 가능 (타인 403)
- [x] 기존 테스트 전체 통과
- [x] 프론트엔드·안드로이드 빌드 성공

---

## 주의사항
- 기존 Redis refresh 토큰에는 role 클레임이 없음 → 배포 시 Redis FLUSHDB 필요
- `schema.sql` DROP 순서 변경 필수 (users → patients)
- PatientService에서 `entityManager.flush()` 후 patient.getId() 사용
- PATIENT의 본인 데이터 접근 제한은 서비스 레이어에서 처리
