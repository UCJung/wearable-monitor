# TASK-13 — Android AGP 9.x / Gradle 9.x 마이그레이션

> 선행: TASK-12 (전체 완료) | 후속: 없음
> 관련: Android 빌드 도구 업그레이드

---

## 작업 목표
AGP(Android Gradle Plugin) 8.3.2 → 9.0.1, Gradle 8.9 → 9.1.0, Kotlin 1.9.23 → 2.3.10 업그레이드에 따른
빌드 오류를 해결하고, deprecated API를 제거하여 정상 빌드를 복원한다.

---

## 현재 오류 분석

### 오류 #1 — `org.jetbrains.kotlin.android` 플러그인 충돌
```
The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.
Solution: Remove the 'org.jetbrains.kotlin.android' plugin from this project's build file.
```
- **원인**: AGP 9.0부터 Kotlin 지원이 AGP에 내장됨
- **해결**: `build.gradle`(루트)와 `app/build.gradle`에서 `org.jetbrains.kotlin.android` 플러그인 제거

### 오류 #2 (예상) — `kotlin-kapt` deprecated → KSP 마이그레이션 필요
- AGP 9.x + Kotlin 2.x 환경에서 `kapt`는 deprecated
- Hilt, Room 등 어노테이션 프로세서를 KSP로 전환 필요

### 경고 #3 — `android.enableJetifier=true` deprecated
- AGP 10.0에서 제거 예정, 현재 deprecated 경고 발생
- AndroidX 전환 완료 상태이므로 제거 가능

### 이슈 #4 (예상) — `kotlinOptions` 블록 변경
- AGP 9.x에서 `kotlinOptions` 블록이 변경될 수 있음
- `compilerOptions`로 마이그레이션 필요 여부 확인

---

## 체크리스트

### 13-1. Gradle Wrapper 버전 확인
- [x] `gradle-wrapper.properties` — Gradle 9.1.0 확인 (AGP 9.0.1 호환)

### 13-2. Kotlin 플러그인 제거
- [x] `android/build.gradle` (루트) — `org.jetbrains.kotlin.android` 플러그인 선언 제거
- [x] `android/app/build.gradle` — `id 'org.jetbrains.kotlin.android'` 제거

### 13-3. kapt → KSP 마이그레이션
- [x] `android/build.gradle` (루트) — KSP 플러그인 추가 (`com.google.devtools.ksp` 2.3.5)
- [x] `android/app/build.gradle` — `id 'kotlin-kapt'` → `id 'com.google.devtools.ksp'` 변경
- [x] `android/app/build.gradle` — 모든 `kapt` 의존성을 `ksp`로 변경
  - `kapt 'com.google.dagger:hilt-compiler'` → `ksp`
  - `kapt 'androidx.room:room-compiler'` → `ksp`
  - `kapt 'androidx.hilt:hilt-compiler'` → `ksp`
- [x] `android/app/build.gradle` — `kapt { correctErrorTypes true }` 블록 제거

### 13-4. Hilt 버전 호환성
- [x] `android/build.gradle` — Hilt 2.59.2 (AGP 9.0 + KSP 호환 확인)
- [x] `android/app/build.gradle` — Hilt 의존성 버전 통일 (2.51.1 → 2.59.2)

### 13-5. Room 버전 호환성
- [x] `android/app/build.gradle` — Room 2.6.1 → 2.7.1 업그레이드 (KSP 지원)

### 13-6. Navigation SafeArgs 호환성
- [x] `android/build.gradle` — `androidx.navigation.safeargs.kotlin` 2.9.7 확인
- [x] `android/app/build.gradle` — Navigation 의존성 2.7.7 → 2.9.7 통일

### 13-7. gradle.properties 정리
- [x] `android/gradle.properties` — `android.enableJetifier=true` 제거
- [x] `android/gradle.properties` — `android.suppressUnsupportedCompileSdk=35` 제거

### 13-8. kotlinOptions → compilerOptions 마이그레이션
- [x] `android/app/build.gradle` — `kotlinOptions { jvmTarget = '17' }` → `kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }` 변경

### 13-9. 기타 의존성 버전 업데이트
- [x] AndroidX 라이브러리 버전 호환성 확인 (기존 버전 호환 유지)
- [ ] 빌드 경고 0건 목표로 버전 조정 (MasterKeys deprecated 등 잔여 경고 있음)

### 13-10. 빌드 검증
- [x] `./gradlew assembleDebug` 성공
- [x] `./gradlew test` 성공 (단위 테스트)
- [ ] 빌드 경고 최소화 확인

---

## 완료 기준
- [x] `./gradlew assembleDebug` BUILD SUCCESSFUL
- [x] `./gradlew test` BUILD SUCCESSFUL
- [x] `org.jetbrains.kotlin.android` 플러그인 완전 제거
- [x] `kapt` → `ksp` 전환 완료
- [x] `android.enableJetifier` 경고 제거
- [x] Hilt / Room / Navigation 의존성 버전 통일 및 호환성 확보

---

## 잔여 빌드 경고 (수동 확인 필요)
- `MasterKeys` deprecated (SecurityModule.kt) — `MasterKey.Builder` API로 전환 권장
- `scaledDensity` deprecated (StepIndicatorView.kt) — 기능에 영향 없음
- `Deprecated Gradle features used` — Gradle 10 호환 경고 (AGP/플러그인 업데이트 시 해소 예정)

---

## 주요 참고 자료
- [AGP 9.0 릴리스 노트](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Built-in Kotlin 마이그레이션](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [kapt → KSP 마이그레이션](https://developer.android.com/build/migrate-to-ksp)
- [AGP 9.0 플러그인 호환성](https://agp-status.frybits.com/agp-9.0.0/)

---

## 주의사항
- AGP 9.x 마이그레이션은 단순 버전 범프가 아닌 구조적 변경이 필요함
- KSP 전환 시 어노테이션 프로세서별 KSP 호환 버전 확인 필수
- 마이그레이션 후 기존 TASK-08~10의 Android 기능이 정상 동작하는지 확인 필요
