# TASK-08 — Android: 초기화 + Health Connect 연동

> 선행: TASK-00 | 후속: TASK-09
> 참조: STYLE_GUIDE_ANDROID.md

---

## 작업 목표
Android 프로젝트 구조를 구성하고 Health Connect 권한 요청 및
14개 수집 항목 데이터 읽기 기능을 구현한다.

---

## 체크리스트

### 8-1. 프로젝트 구조 및 의존성
- [ ] `build.gradle (app)` 의존성 전체
  ```
  hilt-android + kapt, lifecycle-viewmodel-ktx
  kotlinx-coroutines-android, kotlinx-coroutines-core
  retrofit2, okhttp3, logging-interceptor
  room-runtime, room-ktx, room-compiler (kapt)
  datastore-preferences, work-runtime-ktx
  androidx.health.connect:client
  mpandroidchart, security-crypto
  ```
- [ ] `res/values/colors.xml` (STYLE_GUIDE_ANDROID.md 섹션 14 전체)
- [ ] `res/values/dimens.xml` (STYLE_GUIDE_ANDROID.md 섹션 15 전체)
- [ ] `res/values/styles.xml` (TextAppearance 4종)
- [ ] `WearableApplication.kt` (@HiltAndroidApp)

### 8-2. AndroidManifest.xml 설정
- [ ] Health Connect 권한 6종 선언
  ```xml
  READ_HEART_RATE, READ_SLEEP, READ_STEPS,
  READ_OXYGEN_SATURATION, READ_HEART_RATE_VARIABILITY,
  READ_SKIN_TEMPERATURE, READ_RESPIRATORY_RATE,
  READ_TOTAL_CALORIES_BURNED, READ_EXERCISE
  ```
- [ ] Health Connect 패키지 쿼리 선언 (queries)
- [ ] WorkManager 권한
- [ ] Internet 권한

### 8-3. DI 모듈
- [ ] `di/NetworkModule.kt`
  ```kotlin
  @Provides OkHttpClient (로깅 인터셉터 + JWT 인터셉터)
  @Provides Retrofit (baseUrl from BuildConfig)
  @Provides WearableApiService
  ```
- [ ] `di/DatabaseModule.kt`
  ```kotlin
  @Provides AppDatabase (Room)
  @Provides BiometricDao
  @Provides SyncStatusDao
  ```
- [ ] `di/HealthModule.kt`
  ```kotlin
  @Provides HealthConnectClient
  ```

### 8-4. Room DB (로컬 버퍼)
- [ ] `data/local/BiometricEntity.kt`
  ```kotlin
  @Entity(tableName = "biometric_buffer")
  // id, itemCode, measuredAt, valueNumeric, valueText, durationSec,
  // category, unit, isSynced, createdAt
  ```
- [ ] `data/local/SyncStatusEntity.kt`
  ```kotlin
  @Entity(tableName = "sync_status")
  // itemCode, lastSyncedAt (HC 마지막 읽기 시각)
  ```
- [ ] `data/local/BiometricDao.kt`
  ```kotlin
  @Insert(onConflict = REPLACE)
  @Query("SELECT * FROM biometric_buffer WHERE isSynced = 0 LIMIT 500")
  @Query("UPDATE biometric_buffer SET isSynced = 1 WHERE id IN (:ids)")
  @Query("DELETE FROM biometric_buffer WHERE isSynced = 1 AND createdAt < :before")
  ```
- [ ] `data/local/AppDatabase.kt`

### 8-5. HealthConnectManager
- [ ] `health/HealthConnectManager.kt`
  ```kotlin
  // 권한 상태 확인
  suspend fun checkPermissions(): Set<String>

  // 권한 요청 (Activity에서 호출)
  fun getPermissionContract(): ActivityResultContract

  // 14종 수집 메서드 (lastSyncedAt 이후 데이터만)
  suspend fun readHeartRate(start: Instant, end: Instant): List<HeartRateRecord>
  suspend fun readSleepSessions(start: Instant, end: Instant): List<SleepSessionRecord>
  suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord>
  suspend fun readOxygenSaturation(start: Instant, end: Instant): List<OxygenSaturationRecord>
  // ... 나머지 항목들

  // 전체 수집 통합 메서드
  suspend fun collectAllData(lastSyncedAt: Instant): List<BiometricEntity>
  ```

### 8-6. Repository 및 UseCase
- [ ] `data/repository/BiometricRepository.kt` (인터페이스)
- [ ] `data/repository/BiometricRepositoryImpl.kt`
  ```kotlin
  suspend fun collectAndBuffer(): Result<Int>  // HC 읽기 → Room 저장
  suspend fun getPendingData(): List<BiometricEntity>  // 미전송 데이터
  suspend fun markAsSynced(ids: List<Long>)
  suspend fun cleanOldData(daysToKeep: Int = 7)
  ```

---

## Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-08.md를 읽어줘.
docs/STYLE_GUIDE_ANDROID.md도 참고해줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-08
### 생성 파일 목록 (경로 | 역할)
### Health Connect 권한 요청 흐름
### Room DB 설계 (버퍼 테이블 ERD)
### 증분 수집 전략 (lastSyncedAt 관리 방법)
### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-08 승인.

docs/STYLE_GUIDE_ANDROID.md의 colors.xml, dimens.xml을 정확히 적용해줘.
색상·크기 하드코딩 절대 금지.

순서:
1. build.gradle 의존성 + AndroidManifest.xml
2. res/values/ (colors, dimens, styles) 스타일 가이드 기준
3. DI 모듈 3종 (Network, Database, Health)
4. Room DB (BiometricEntity, SyncStatusEntity, Dao, AppDatabase)
5. HealthConnectManager (권한 확인 + 14종 수집 메서드)
6. BiometricRepositoryImpl (HC읽기 → Room버퍼 저장)

Health Connect는 코루틴 suspend 함수로 구현해줘.
```

### Step 3 — 완료 검증
```
TASK-08 완료 검증해줘.

1. Android 빌드 오류 0건 확인
2. colors.xml에 @color/primary 값 #6B5CE7 확인
3. dimens.xml에 @dimen/card_corner_radius 14dp 확인
4. Room DB 마이그레이션 전략 확인
5. HealthConnectManager.collectAllData() 메서드 시그니처 확인
6. 에뮬레이터에서 Health Connect 권한 요청 팝업 동작 확인 (가능한 경우)
```

---

## 완료 기준
- [ ] Android 빌드 오류 0건
- [ ] `colors.xml`, `dimens.xml` 스타일 가이드 100% 적용
- [ ] Health Connect 권한 9종 Manifest 선언
- [ ] Room DB 테이블 2종 생성
- [ ] HealthConnectManager 14종 수집 메서드 구현
- [ ] BiometricRepository 4개 메서드 구현
