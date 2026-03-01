# TASK-09 — Android: 인증 + 설정 Wizard

> 선행: TASK-08, TASK-02 | 후속: TASK-10
> 관련 기능: FNT-007 (인증), FNT-008 (설정 가이드), FNT-009 (단계별 설정 Wizard)

---

## 작업 목표
로그인 화면(JWT 저장), 설정 가이드(5단계 가이드), 단계별 설정 Wizard를 구현한다.

---

## 체크리스트

### 9-1. Retrofit API Service
- [ ] `data/remote/WearableApiService.kt`
  ```kotlin
  @POST("/api/v1/auth/login")
  suspend fun login(@Body req: LoginRequest): Response<ApiResponse<TokenResponse>>

  @POST("/api/v1/biometric/batch")
  suspend fun uploadBatch(@Body req: BatchUploadRequest): Response<ApiResponse<BatchUploadResponse>>
  ```
- [ ] `data/remote/dto/LoginRequest.kt` / `TokenResponse.kt`
- [ ] `data/remote/dto/BatchUploadRequest.kt` / `BiometricUploadItem.kt`

### 9-2. 인증 (FNT-007)
- [ ] `data/repository/AuthRepository.kt`
  ```kotlin
  suspend fun login(patientId: String, password: String): Result<TokenResponse>
  fun saveToken(token: String)         // EncryptedSharedPreferences
  fun getToken(): String?
  fun clearToken()
  fun isLoggedIn(): Boolean
  ```
- [ ] `ui/login/LoginViewModel.kt`
  ```kotlin
  val uiState: StateFlow<LoginUiState>
  fun login(patientId: String, password: String)
  // LoginUiState: Idle | Loading | Success | Error(message, remainAttempts)
  ```
- [ ] `ui/login/LoginActivity.kt`
  - 배경: 그라디언트 (#F5F3FF → #FFFFFF)
  - 로고 48sp + 앱명 20sp + 서브타이틀
  - patientId / 비밀번호 TextInputLayout (OutlinedBox, 10dp 모서리)
  - 비밀번호 눈 아이콘 토글
  - [로그인] 버튼 (48dp, primary, 12dp 모서리)
  - 오류 시: 에러 텍스트 + 빨간 배너 (danger 배경)
  - 로그인 성공 → DashboardActivity 이동 + 현재 Activity finish()
  - 앱 시작 시 토큰 유효 → 자동 로그인 (LoginActivity 스킵)

### 9-3. 설정 가이드 (FNT-008)
- [ ] `ui/setup/GuideActivity.kt` (ViewPager2 기반)
- [ ] `ui/setup/GuidePagerAdapter.kt`
- [ ] `ui/setup/GuideFragment.kt` (각 단계 공통 Fragment)
  - 5단계: 페어링 → Samsung Health → Health Connect → 항목설정 → 배터리 최적화
  - 각 단계: 제목(15sp) + 설명(13sp) + 이미지/GIF placeholder
  - 하단 [이전] [다음] 버튼 (Wizard와 동일 스타일)
  - 완료 단계 DataStore에 저장
  - 마지막 단계 완료 → SetupWizardActivity 이동

### 9-4. 단계별 설정 Wizard (FNT-009)
- [ ] `ui/setup/SetupWizardActivity.kt`
- [ ] `ui/setup/SetupWizardViewModel.kt`
  ```kotlin
  val currentStep: StateFlow<Int>  // 1~5
  val stepStates: StateFlow<Map<Int, StepState>>  // DONE/CURRENT/TODO
  val canComplete: StateFlow<Boolean>  // 필수 단계(1,2,5) 모두 완료 여부
  ```
- [ ] `ui/setup/StepIndicatorView.kt` (커스텀 뷰)
  - 완료: 흰 배경 + primary 체크, 현재: 흰 배경 + 4dp 글로우, 미완료: 투명 25%
  - 연결선: 완료=흰 80%, 미완료=흰 30%
- [ ] STEP별 Fragment 5종
  ```
  Step1Fragment: Health Connect 권한 목록 → [권한 허용하기] → requestPermissions()
  Step2Fragment: 워치 연결 자동 감지 → 연결됨/미연결 상태 카드
  Step3Fragment: Samsung Health 앱 이동 버튼 (심박수 연속 측정 안내)
  Step4Fragment: Samsung Health 앱 이동 버튼 (수면 SpO2 활성화 안내)
  Step5Fragment: ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS Intent
  ```
- [ ] 필수 단계(1, 2, 5) 미완료 시 [완료] 버튼 비활성화
- [ ] 선택 단계(3, 4) 미완료도 다음 이동 가능
- [ ] 모든 필수 단계 완료 → DashboardActivity 이동

---

## Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-09.md를 읽어줘.
docs/STYLE_GUIDE_ANDROID.md와 docs/ui_app_screens.html도 참고해줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-09
### 생성 파일 목록 (경로 | 역할)
### 로그인 자동 처리 흐름 (SplashActivity 포함 여부)
### StepIndicatorView 구현 방식 (Canvas vs View 조합)
### Wizard 단계 상태 저장 방식 (DataStore or ViewModel)
### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-09 승인.

docs/ui_app_screens.html의 앱 화면 시안을 참고해서 구현해줘.
모든 색상 @color/, 크기 @dimen/ 참조 사용. 하드코딩 금지.

순서:
1. Retrofit API Service + DTO
2. AuthRepository (EncryptedSharedPreferences JWT 저장)
3. LoginActivity + LoginViewModel
   - 그라디언트 배경, TextInputLayout, 오류 상태
4. GuideActivity (ViewPager2 5단계)
5. SetupWizardActivity + ViewModel + StepIndicatorView
   - 5개 Step Fragment 각각 구현
   - 필수 단계 완료 여부 체크

JWT EncryptedSharedPreferences 키 이름: "jwt_access_token"
```

### Step 3 — 완료 검증
```
TASK-09 완료 검증해줘.

1. LoginActivity UI 렌더링 확인 (그라디언트, 버튼 스타일)
2. 로그인 성공 → JWT EncryptedSharedPreferences 저장 확인
3. 앱 재실행 → 토큰 있으면 자동 로그인 확인
4. SetupWizardActivity Step1 권한 요청 팝업 확인
5. 필수 단계 미완료 시 [완료] 버튼 disabled 확인
6. Android 빌드 오류 0건 확인
```

---

## 완료 기준
- [ ] LoginActivity UI 스타일 가이드 적용
- [ ] JWT EncryptedSharedPreferences 저장·읽기 동작
- [ ] 자동 로그인 (토큰 유효 시 스킵) 동작
- [ ] ViewPager2 설정 가이드 5단계 동작
- [ ] StepIndicatorView 상태별 렌더링 (DONE/CURRENT/TODO)
- [ ] 필수 단계 완료 검증 로직 동작
- [ ] 빌드 오류 0건
