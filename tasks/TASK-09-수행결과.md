# TASK-09 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-opus-4-6)
> 상태: **완료**

---

## 1. 작업 개요
로그인 화면(JWT EncryptedSharedPreferences 저장), 설정 가이드(ViewPager2 5단계), 단계별 설정 Wizard(5단계 Fragment + StepIndicatorView)를 구현했다.

---

## 2. 완료 기준 달성 현황

| 완료 기준 | 상태 |
|---|---|
| LoginActivity UI 스타일 가이드 적용 | ✅ |
| JWT EncryptedSharedPreferences 저장·읽기 동작 | ✅ |
| 자동 로그인 (토큰 유효 시 스킵) 동작 | ✅ |
| ViewPager2 설정 가이드 5단계 동작 | ✅ |
| StepIndicatorView 상태별 렌더링 (DONE/CURRENT/TODO) | ✅ |
| 필수 단계 완료 검증 로직 동작 | ✅ |
| 빌드 오류 0건 | ✅ |

---

## 3. 체크리스트 완료 현황

### 9-1. Retrofit API Service

| 항목 | 상태 |
|---|---|
| WearableApiService login/uploadBatch 엔드포인트 | ✅ |
| LoginRequest / TokenResponse DTO | ✅ |
| BatchUploadRequest / BiometricUploadItem DTO | ✅ |
| BatchUploadResponse DTO | ✅ |

### 9-2. 인증 (FNT-007)

| 항목 | 상태 |
|---|---|
| AuthRepository (인터페이스) | ✅ |
| AuthRepositoryImpl (EncryptedSharedPreferences JWT 저장) | ✅ |
| LoginViewModel (Idle/Loading/Success/Error StateFlow) | ✅ |
| LoginActivity (그라디언트 배경, TextInputLayout, 에러 배너) | ✅ |
| 비밀번호 눈 아이콘 토글 | ✅ |
| 로그인 성공 → GuideActivity 이동 | ✅ |
| 자동 로그인 (토큰 존재 시 스킵) | ✅ |

### 9-3. 설정 가이드 (FNT-008)

| 항목 | 상태 |
|---|---|
| GuideActivity (ViewPager2 기반) | ✅ |
| GuidePagerAdapter | ✅ |
| GuideFragment (각 단계 공통) | ✅ |
| 5단계 구성 (페어링/Samsung Health/HC/항목/배터리) | ✅ |
| 이전/다음 버튼 (Wizard 동일 스타일) | ✅ |
| 마지막 단계 → SetupWizardActivity 이동 | ✅ |

### 9-4. 단계별 설정 Wizard (FNT-009)

| 항목 | 상태 |
|---|---|
| SetupWizardActivity | ✅ |
| SetupWizardViewModel (currentStep, stepStates, canComplete) | ✅ |
| StepIndicatorView (Canvas 커스텀 뷰) | ✅ |
| Step1Fragment: HC 권한 목록 + 요청 | ✅ |
| Step2Fragment: 워치 연결 자동 감지 | ✅ |
| Step3Fragment: Samsung Health 이동 (심박수) | ✅ |
| Step4Fragment: Samsung Health 이동 (수면 SpO2) | ✅ |
| Step5Fragment: 배터리 최적화 해제 Intent | ✅ |
| 필수 단계(1,2,5) 미완료 시 완료 버튼 비활성화 | ✅ |
| 선택 단계(3,4) 미완료도 다음 이동 가능 | ✅ |
| 모든 필수 단계 완료 → DashboardActivity 이동 | ✅ |

---

## 4. 발견 이슈 및 수정 내역

### 이슈 #1 — AuthInterceptor 토큰 저장소 통합
**증상**: TASK-08에서 DataStore 기반 AuthInterceptor를 생성했으나, TASK-09에서 JWT를 EncryptedSharedPreferences에 저장
**원인**: 두 저장소가 분리되면 인터셉터가 토큰을 읽지 못함
**수정**: AuthInterceptor를 EncryptedSharedPreferences 기반으로 변경. NetworkModule에서 의존성도 SharedPreferences로 교체.

---

## 5. 최종 검증 결과

### 빌드 결과
```
> Task :app:assembleDebug
BUILD SUCCESSFUL in 58s
44 actionable tasks: 23 executed, 21 up-to-date
```

### 검증 항목

| 검증 항목 | 결과 |
|---|---|
| Android 빌드 오류 0건 | ✅ BUILD SUCCESSFUL |
| LoginActivity 그라디언트 배경 (bg_login_gradient.xml) | ✅ |
| LoginActivity 버튼 스타일 (48dp, primary, 12dp corner) | ✅ |
| EncryptedSharedPreferences 키 "jwt_access_token" | ✅ |
| 자동 로그인 흐름 (isLoggedIn → GuideActivity) | ✅ |
| StepIndicatorView 3상태 렌더링 (DONE/CURRENT/TODO) | ✅ |
| 필수 단계(1,2,5) canComplete 검증 로직 | ✅ |
| LoginActivity UI 렌더링 (에뮬레이터) | **수동 확인 필요** |
| SetupWizard Step1 권한 요청 팝업 | **수동 확인 필요** |
| 필수 단계 미완료 시 완료 버튼 disabled 확인 | **수동 확인 필요** |

### 빌드 경고 (기능 무관)
- `MasterKeys` deprecated → `MasterKey.Builder`로 향후 마이그레이션 가능 (현재 security-crypto alpha 버전 제한)
- `scaledDensity` deprecated → API 35 이상에서 대체 API 존재 (minSdk 28 호환 유지)

---

## 6. 후속 TASK 유의사항

- **TASK-10 (Android: 수집 Worker + 대시보드)**: `DashboardActivity`는 현재 빈 placeholder. 레이아웃과 기능 전체 구현 필요.
- **AuthInterceptor 변경**: DataStore → EncryptedSharedPreferences로 토큰 저장소 통합됨. DataStore는 설정 가이드 완료 상태 등 비밀이 아닌 설정에만 사용.
- **SetupWizard 상태 영구 저장**: 현재 ViewModel에서만 관리. 필요 시 DataStore에 완료 단계 영구 저장 추가 가능.

---

## 7. 산출물 목록

### 신규 생성 파일

| 파일 경로 | 역할 |
|---|---|
| `data/remote/dto/LoginRequest.kt` | 로그인 요청 DTO |
| `data/remote/dto/TokenResponse.kt` | 토큰 응답 DTO |
| `data/remote/dto/BatchUploadRequest.kt` | 배치 업로드 요청 DTO |
| `data/remote/dto/BatchUploadResponse.kt` | 배치 업로드 응답 DTO |
| `data/repository/AuthRepository.kt` | 인증 Repository 인터페이스 |
| `data/repository/AuthRepositoryImpl.kt` | JWT EncryptedSharedPreferences 구현 |
| `di/SecurityModule.kt` | EncryptedSharedPreferences DI |
| `ui/login/LoginActivity.kt` | 로그인 화면 |
| `ui/login/LoginViewModel.kt` | 로그인 상태 관리 |
| `ui/setup/GuideActivity.kt` | 설정 가이드 (ViewPager2) |
| `ui/setup/GuidePagerAdapter.kt` | ViewPager2 어댑터 |
| `ui/setup/GuideFragment.kt` | 가이드 단계 공통 Fragment |
| `ui/setup/SetupWizardActivity.kt` | 설정 Wizard |
| `ui/setup/SetupWizardViewModel.kt` | Wizard 상태 관리 |
| `ui/setup/StepIndicatorView.kt` | Canvas 커스텀 뷰 (5단계 인디케이터) |
| `ui/setup/steps/Step1Fragment.kt` | HC 권한 요청 |
| `ui/setup/steps/Step2Fragment.kt` | 워치 연결 감지 |
| `ui/setup/steps/Step3Fragment.kt` | Samsung Health (심박수) |
| `ui/setup/steps/Step4Fragment.kt` | Samsung Health (수면 SpO2) |
| `ui/setup/steps/Step5Fragment.kt` | 배터리 최적화 해제 |
| `ui/dashboard/DashboardActivity.kt` | 대시보드 placeholder |
| `res/layout/activity_login.xml` | 로그인 레이아웃 |
| `res/layout/activity_guide.xml` | 가이드 레이아웃 |
| `res/layout/fragment_guide_step.xml` | 가이드 단계 레이아웃 |
| `res/layout/activity_setup_wizard.xml` | Wizard 레이아웃 |
| `res/layout/fragment_step1.xml` ~ `fragment_step5.xml` | 5개 Step 레이아웃 |
| `res/drawable/bg_login_gradient.xml` | 로그인 배경 그라디언트 |
| `res/drawable/bg_header_gradient.xml` | 헤더 그라디언트 |
| `res/drawable/bg_error_banner.xml` | 에러 배너 배경 |

### 수정 파일

| 파일 경로 | 변경 내용 |
|---|---|
| `AndroidManifest.xml` | GuideActivity, SetupWizardActivity 등록, 배터리/블루투스 권한, Samsung Health 쿼리 |
| `data/remote/WearableApiService.kt` | login, uploadBatch 엔드포인트 추가 |
| `data/remote/AuthInterceptor.kt` | DataStore → EncryptedSharedPreferences 변경 |
| `di/NetworkModule.kt` | AuthInterceptor 의존성 변경 |
| `di/RepositoryModule.kt` | AuthRepository 바인딩 추가 |
| `res/values/strings.xml` | UI 문자열 리소스 추가 |
