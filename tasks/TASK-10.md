# TASK-10 — Android: 수집 Worker + 현황 대시보드

> 선행: TASK-09, TASK-03 | 후속: TASK-11
> 관련 기능: FNT-010 (현황 대시보드)

---

## 작업 목표
WorkManager 30분 배치 업로드 Worker와 현황 대시보드(카드 그리드, 게이지) 화면을 구현한다.

---

## 체크리스트

### 10-1. SyncWorker (WorkManager)
- [ ] `worker/SyncWorker.kt`
  ```kotlin
  // 동작 흐름:
  // 1. HealthConnectManager.collectAllData(lastSyncedAt) 호출
  // 2. Room BiometricBuffer에 저장
  // 3. BiometricBuffer에서 미전송 데이터 최대 500건 조회
  // 4. POST /api/v1/biometric/batch 전송
  // 5. 성공 시 isSynced = true + lastSyncedAt 업데이트
  // 6. 실패 시 지수 백오프 재시도 (최대 3회)
  // 7. 7일 이상 된 synced 데이터 자동 삭제

  override suspend fun doWork(): Result
  ```
- [ ] `worker/SyncWorkerScheduler.kt`
  ```kotlin
  // 30분 주기 PeriodicWorkRequest
  // Constraints: NetworkType.CONNECTED
  // ExistingPeriodicWorkPolicy.KEEP (중복 등록 방지)
  fun scheduleSync(context: Context)
  fun cancelSync(context: Context)
  ```
- [ ] SetupWizard 완료 시 SyncWorker 등록

### 10-2. 대시보드 화면 (FNT-010)
- [ ] `ui/dashboard/DashboardFragment.kt`
- [ ] `ui/dashboard/DashboardViewModel.kt`
  ```kotlin
  val uiState: StateFlow<DashboardUiState>
  val selectedDate: StateFlow<LocalDate>  // 기본: 오늘
  fun selectDate(date: LocalDate)
  fun refresh()  // Pull-to-Refresh → HC 재읽기 + 서버 업로드 시도

  // DashboardUiState:
  // watchConnected: Boolean
  // lastSyncAt: String
  // vitalData: VitalData?  (심박수·SpO2·스트레스·피부온도)
  // activityData: ActivityData?  (걸음수·칼로리·운동)
  // sleepData: SleepData?  (시간·점수·단계)
  // energyScore: Int?
  // isDeviceAssigned: Boolean  // 미할당 시 경고
  ```

### 10-3. 데이터 카드 UI

- [ ] **헤더** (그라디언트 배경: #6B5CE7 → #8A7EE8)
  - "안녕하세요, {이름}님 👋" 17sp + 날짜 12sp
  - 우측: 워치 연결 배지 (연결됨=#4ADE80 점, 끊김=#FF6B6B 점)
  - 하단: "마지막 동기화: {time}" (1h 초과=노랑, 3h 초과=빨강)

- [ ] **날짜 선택 바** (오늘 기준 최대 30일 이전)

- [ ] **카드 그리드** (2열, gap 8dp)
  - `DataCardView.kt` (재사용 커스텀 뷰 or ViewHolder)
  - 데이터 없을 시 "—" 표시 + alpha 0.5
  - 카드 배경: 항목 분류별 색상 (VITAL=#FFFDE7, ACTIVITY=#E3F2FD, SLEEP=#F3E5F5, AI=#E8F5E9)

- [ ] **걸음 수 카드** (full-width)
  - 수치 + LinearProgressIndicator (목표 10,000보 기준)
  - 색상: primary → primary_dark 그라디언트
  - 퍼센트 우측 표시

- [ ] **수면 단계 카드** (full-width)
  - 수면 시간 + 수면 점수 배지
  - 가로 세그먼트 바 (깊은잠=#5647CC / REM=#8A7EE8 / 얕은잠=#E0E0E0)
  - 범례 (10sp)

- [ ] **에너지 점수 카드** (full-width)
  - 원형 게이지 (64×64dp, stroke 6dp)
    - 배경 원: #E8E0FF
    - 진행 원: primary, strokeLineCap=round
    - 내부 숫자: 15sp bold primary
  - 점수별 색상: 80+=ok, 50-79=accent, 50-=danger

- [ ] **Pull-to-Refresh** (SwipeRefreshLayout)
  - 당기면: HC 최신 데이터 재읽기 + 서버 배치 업로드
  - 완료 시 refreshing=false

### 10-4. 경고 상태 처리
- [ ] 장치 미할당: [담당자에게 문의하세요] 배너 + [설정 화면으로 이동] 버튼
- [ ] 워치 미연결: [워치 연결하기] 배너 (warn 배경)
- [ ] Health Connect 권한 없음: [설정으로 이동] 버튼
- [ ] 배터리 20% 미만: 배터리 경고 배너 (danger 배경)

---

## Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-10.md를 읽어줘.
docs/STYLE_GUIDE_ANDROID.md와 docs/ui_app_screens.html도 참고해줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-10
### 생성 파일 목록 (경로 | 역할)
### WorkManager 실행 보장 전략 (doze mode, 배터리 최적화)
### 대시보드 카드 그리드 레이아웃 방식
  (RecyclerView + SpanSizeLookup vs 고정 ScrollView)
### 원형 게이지 구현 방식 (Canvas 직접 그리기 vs MPAndroidChart)
### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-10 승인.

docs/ui_app_screens.html 시안을 정확히 따라서 구현해줘.
모든 색상 @color/, 크기 @dimen/ 참조. 하드코딩 금지.

순서:
1. SyncWorker (HC읽기 → Room저장 → 서버전송 → 정리)
2. SyncWorkerScheduler (30분 주기, CONNECTED 제약)
3. DashboardViewModel (StateFlow 기반 UiState)
4. DashboardFragment UI
   - 헤더 그라디언트 + 연결 배지
   - 카드 그리드 (2열)
   - 걸음 수 진행 바 카드
   - 수면 단계 세그먼트 바 카드
   - 에너지 점수 원형 게이지 카드
5. 경고 상태 배너 (미할당/미연결/권한없음/배터리)
6. Pull-to-Refresh

원형 게이지는 Canvas로 직접 그려줘 (MPAndroidChart 오버스펙).
```

### Step 3 — 완료 검증
```
TASK-10 완료 검증해줘.

1. WorkManager 30분 주기 등록 확인 (WorkInfo 조회)
2. SyncWorker doWork() 실행 로그 확인
3. DashboardFragment 렌더링 (에뮬레이터 또는 단위 테스트)
4. 에너지 점수 80+ → ok 색상 / 50 미만 → danger 색상 확인
5. Pull-to-Refresh 동작 확인
6. 장치 미할당 시 경고 배너 표시 확인
7. Android 빌드 오류 0건 확인
```

---

## 완료 기준
- [ ] SyncWorker 30분 주기 정상 등록
- [ ] HC → Room → 서버 업로드 흐름 동작
- [ ] 대시보드 카드 그리드 렌더링
- [ ] 걸음 수 진행 바 정확한 퍼센트
- [ ] 수면 단계 세그먼트 바 비율 표시
- [ ] 에너지 점수 원형 게이지 점수별 색상 분기
- [ ] Pull-to-Refresh 동작
- [ ] 경고 배너 4종 조건부 표시
- [ ] 빌드 오류 0건
