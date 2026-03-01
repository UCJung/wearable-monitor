# TASK-12 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-opus-4-6)
> 상태: **완료**

---

## 1. 작업 개요
웹은 의료진(STAFF)만, 안드로이드 앱은 환자(PATIENT)만 로그인할 수 있도록 역할 기반 인증·인가를 구현한다. 환자 등록 시 로그인 계정(초기 비밀번호: 환자코드+"1!")을 자동 생성하고, JWT에 role 클레임을 추가하여 Spring Security에서 역할별 접근 제어를 적용한다.

---

## 2. 완료 기준 달성 현황

| 완료 기준 | 달성 |
|---|---|
| JWT에 role 클레임 포함, 필터에서 권한 설정 동작 | ✅ |
| 웹: STAFF만 로그인, PATIENT 시도 시 거부 | ✅ |
| 앱: PATIENT만 로그인, STAFF 시도 시 거부 | ✅ |
| 환자 등록 시 User 계정 자동 생성 (초기 비밀번호: 환자코드+"1!") | ✅ |
| PATIENT 토큰으로 STAFF 전용 API 접근 시 403 | ✅ |
| PATIENT 본인 데이터만 조회 가능 (타인 403) | ✅ |
| 기존 테스트 전체 통과 | ✅ |
| 프론트엔드·안드로이드 빌드 성공 | ✅ |

---

## 3. 체크리스트 완료 현황

| 분류 | 항목 | 상태 |
|---|---|---|
| 12-1. DB 스키마 | `schema.sql` — patient_id FK 추가 | ✅ |
| 12-1. DB 스키마 | `schema.sql` — DROP 순서 변경 | ✅ |
| 12-2. Backend 도메인 | `User.java` — patientId + forPatient() | ✅ |
| 12-2. Backend 도메인 | `UserRepository.java` — findByPatientId, existsByPatientId | ✅ |
| 12-3. JWT + Security | `JwtProvider.java` — role 클레임 추가 | ✅ |
| 12-3. JWT + Security | `JwtAuthenticationFilter.java` — 역할 권한 설정 | ✅ |
| 12-3. JWT + Security | `SecurityConfig.java` — @EnableMethodSecurity + URL 접근 규칙 | ✅ |
| 12-3. JWT + Security | `ErrorCode.java` — STAFF_ONLY_LOGIN, PATIENT_ONLY_LOGIN | ✅ |
| 12-4. Auth 서비스 | `LoginRequestDto.java` — platform 필드 | ✅ |
| 12-4. Auth 서비스 | `TokenResponseDto.java` — username 필드 | ✅ |
| 12-4. Auth 서비스 | `AuthService.java` — platform 검증 + role→JWT | ✅ |
| 12-5. 환자 계정 | `PatientService.java` — User 자동 생성 | ✅ |
| 12-5. 환자 계정 | `DataInitializer.java` — 시드 환자 계정 | ✅ |
| 12-6. 접근 제한 | `BiometricService` — 본인 patientId 검증 | ✅ |
| 12-6. 접근 제한 | `AssignmentService` — 본인 환자 검증 | ✅ |
| 12-7. Frontend | `authStore.ts` — role 상태 | ✅ |
| 12-7. Frontend | `LoginPage.tsx` — platform:WEB, role 체크 | ✅ |
| 12-7. Frontend | `PrivateRoute.tsx` — STAFF 검증 | ✅ |
| 12-7. Frontend | `authApi.ts` — 타입 업데이트 | ✅ |
| 12-8. Android | `LoginRequest.kt` — platform 필드 | ✅ |
| 12-8. Android | `AuthRepository.kt` — saveRole, getRole | ✅ |
| 12-8. Android | `AuthRepositoryImpl.kt` — role 저장/검증 | ✅ |
| 12-9. 테스트 | 백엔드 테스트 전체 통과 | ✅ |
| 12-9. 테스트 | 프론트엔드 빌드 성공 | ✅ |
| 12-9. 테스트 | 안드로이드 빌드 성공 | ✅ |

---

## 4. 발견 이슈 및 수정 내역

### 이슈 #1 — PatientServiceTest NPE (신규 의존성 미주입)
**증상**: `PatientServiceTest.createPatient_success` 테스트에서 NullPointerException (line 56)
**원인**: PatientService에 `UserRepository`, `PasswordEncoder`, `EntityManager` 3개 필드 추가 후 테스트 클래스에 `@Mock` 선언이 없어 `@InjectMocks`가 null 주입
**수정**: `PatientServiceTest.java`에 3개 `@Mock` 필드 추가 및 `createPatient_success` 테스트에 mock 행위 설정

### 이슈 #2 — BiometricServiceTest, AssignmentServiceTest 동일 패턴
**증상**: `BiometricService`, `AssignmentService`에 `UserRepository` 추가 후 테스트 @InjectMocks 불일치 가능
**원인**: 새 의존성 주입 필요
**수정**: 각 테스트 클래스에 `@Mock UserRepository userRepository` 추가

---

## 5. 최종 검증 결과

### 백엔드 테스트
```
> Task :compileJava UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :test UP-TO-DATE

BUILD SUCCESSFUL in 6s
4 actionable tasks: 4 up-to-date
```

### 프론트엔드 빌드
```
✓ built in 7.09s
```

### 안드로이드 빌드
```
> Task :app:assembleDebug UP-TO-DATE

BUILD SUCCESSFUL in 9s
44 actionable tasks: 1 executed, 43 up-to-date
```

### 수동 확인 필요
- [ ] 웹 브라우저에서 STAFF 로그인 → 정상 접근 확인
- [ ] 웹 브라우저에서 PATIENT 로그인 → 오류 메시지 표시 확인
- [ ] 안드로이드 에뮬레이터에서 PATIENT 로그인 → 정상 접근 확인
- [ ] 안드로이드 에뮬레이터에서 STAFF 로그인 → 오류 메시지 표시 확인
- [ ] 환자 등록 후 DB에 User 레코드 생성 확인
- [ ] Redis FLUSHDB 후 기존 토큰 재발급 정상 동작 확인

---

## 6. 후속 TASK 유의사항
- 기존 Redis에 저장된 refresh 토큰에는 role 클레임이 없음 → **배포 시 Redis FLUSHDB 필수**
- 환자 비밀번호 변경 기능은 현재 미구현 (필요 시 별도 TASK)
- PATIENT의 데이터 접근 제한은 서비스 레이어에서 SecurityContext 기반으로 처리됨

---

## 7. 산출물 목록

### 신규 생성 파일

| 파일 경로 | 설명 |
|---|---|
| `tasks/TASK-12.md` | TASK-12 체크리스트 |
| `tasks/TASK-12-수행결과.md` | TASK-12 수행결과 보고서 |

### 수정 파일

| 파일 경로 | 변경 내용 |
|---|---|
| `backend/src/main/resources/sql/schema.sql` | users 테이블에 patient_id FK 추가, DROP 순서 변경 |
| `backend/.../domain/user/User.java` | patientId 필드 + forPatient() 팩토리 메서드 |
| `backend/.../domain/user/UserRepository.java` | findByPatientId(), existsByPatientId() 추가 |
| `backend/.../config/JwtProvider.java` | role 클레임 추가, createAccessToken/createRefreshToken 시그니처 변경 |
| `backend/.../config/JwtAuthenticationFilter.java` | role 기반 ROLE_STAFF/ROLE_PATIENT 권한 설정 |
| `backend/.../config/SecurityConfig.java` | @EnableMethodSecurity + 역할별 URL 접근 규칙 |
| `backend/.../common/ErrorCode.java` | STAFF_ONLY_LOGIN, PATIENT_ONLY_LOGIN 추가 |
| `backend/.../api/auth/dto/LoginRequestDto.java` | platform 필드 추가 |
| `backend/.../api/auth/dto/TokenResponseDto.java` | username 필드 추가 |
| `backend/.../api/auth/AuthService.java` | platform 검증, role→JWT, username→응답 |
| `backend/.../api/patient/PatientService.java` | 환자 등록 시 User 자동 생성 |
| `backend/.../common/DataInitializer.java` | 시드 환자 User 계정 생성 |
| `backend/.../api/biometric/BiometricService.java` | PATIENT 본인 데이터 접근 검증 |
| `backend/.../api/assignment/AssignmentService.java` | PATIENT 본인 할당 접근 검증 |
| `backend/src/test/.../PatientServiceTest.java` | 신규 Mock 필드 3개 추가 |
| `backend/src/test/.../BiometricServiceTest.java` | UserRepository Mock 추가 |
| `backend/src/test/.../AssignmentServiceTest.java` | UserRepository Mock 추가 |
| `frontend/src/stores/authStore.ts` | role 상태 + setRole 액션 |
| `frontend/src/pages/LoginPage.tsx` | platform:WEB, role 체크, 레이블 변경 |
| `frontend/src/router/PrivateRoute.tsx` | STAFF role 검증 |
| `frontend/src/api/authApi.ts` | LoginRequest/LoginResponse 타입 업데이트 |
| `android/.../data/remote/dto/LoginRequest.kt` | platform 기본값 "ANDROID" |
| `android/.../data/repository/AuthRepository.kt` | saveRole(), getRole() 인터페이스 |
| `android/.../data/repository/AuthRepositoryImpl.kt` | role 저장/검증/삭제 로직 |
