# TASK-00 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-sonnet-4-6)
> 상태: **완료**

---

## 1. 작업 개요

모노레포 구조 생성, Back-end / Front-end / Android 각 파트의 기본 뼈대와
공통 설정(색상·응답·예외·JWT)을 구성한다.

---

## 2. 완료 기준 달성 현황

| 완료 기준 | 결과 |
|---|---|
| `./gradlew build` 성공 | ✅ |
| `npm run dev` 정상 구동 (localhost:5173) | ✅ |
| `docker-compose up` PostgreSQL + Redis 정상 기동 | ✅ |
| Android 빌드 오류 0건 | ✅ |
| `global.css` CSS 변수 전체 선언 확인 | ✅ |
| `colors.xml`, `dimens.xml` 스타일 가이드 기준 적용 | ✅ |

---

## 3. 체크리스트 완료 현황

### 0-1. 모노레포 구조

| 항목 | 상태 |
|---|---|
| `wearable-monitor/` 루트 생성 | ✅ |
| `docs/` 설계 문서 배치 | ✅ |
| `tasks/` TASK MD 파일 배치 | ✅ |
| `.gitignore` (Java + Node + Android + IDE + `.env`) | ✅ |
| 루트 `README.md` (프로젝트 설명 + 실행 방법) | ✅ |
| `docker-compose.yml` (PostgreSQL 16 + TimescaleDB + Redis 7) | ✅ |

### 0-2. Back-end 초기화

| 항목 | 상태 |
|---|---|
| `backend/build.gradle` 의존성 정의 | ✅ |
| `application.yml` + `application-dev.yml` | ✅ |
| `ApiResponse<T>` 공통 응답 래퍼 | ✅ |
| `ErrorCode` enum | ✅ |
| `WearableException` + `GlobalExceptionHandler` | ✅ |
| `JwtProvider` (토큰 생성·검증·만료 처리) | ✅ |
| `SecurityConfig` (공개 엔드포인트, CORS 설정) | ✅ |
| `JpaAuditingConfig` (@EnableJpaAuditing) | ✅ |
| `QueryDslConfig` (JPAQueryFactory Bean) | ✅ |
| `RedisConfig` (RedisTemplate Bean) | ✅ |

### 0-3. Front-end 초기화

| 항목 | 상태 |
|---|---|
| Vite + React + TypeScript 프로젝트 생성 | ✅ |
| 패키지 설치 (Tailwind, Zustand, TanStack Query, Axios 등) | ✅ |
| `tailwind.config.js` CSS 변수 연동 | ✅ |
| `src/styles/global.css` CSS 변수 전체 선언 | ✅ |
| `src/api/axiosInstance.ts` JWT 인터셉터 | ✅ |
| `src/stores/authStore.ts` Zustand 인증 스토어 | ✅ |
| `src/stores/toastStore.ts` 토스트 알림 스토어 | ✅ |
| `src/components/layout/Layout.tsx` | ✅ |
| `src/components/layout/Sidebar.tsx` | ✅ |
| `src/components/layout/Header.tsx` | ✅ |
| `src/router/index.tsx` | ✅ |
| `src/types/common.ts` | ✅ |

### 0-4. Android 초기화

| 항목 | 상태 |
|---|---|
| Android 프로젝트 생성 (minSdk 28, Kotlin) | ✅ |
| `build.gradle (app)` 의존성 정의 | ✅ |
| `res/values/colors.xml` (스타일 가이드 기준) | ✅ |
| `res/values/dimens.xml` (스타일 가이드 기준) | ✅ |
| `res/values/styles.xml` (TextAppearance 정의) | ✅ |
| `AndroidManifest.xml` Health Connect 권한 6종 | ✅ |
| `WearableApplication.kt` (@HiltAndroidApp) | ✅ |
| `di/NetworkModule.kt` (Retrofit + OkHttp DI) | ✅ |
| `di/DatabaseModule.kt` (Room DI) | ✅ |

---

## 4. 검증 중 발견된 이슈 및 수정 내역

검증 과정에서 총 **8건**의 이슈가 발견되어 즉시 수정 완료하였다.

---

### 이슈 #1 — Redis 포트 바인딩 실패

**증상**
```
Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:6379
-> 127.0.0.1:0: listen tcp 0.0.0.0:6379: bind: An attempt was made to access a socket
   in a way forbidden by its access permissions.
```

**원인**
Windows `netsh` 예약 포트 범위(6340~6739)에 6379가 포함되어 있어
Docker가 해당 포트에 바인딩 불가.

**수정 파일**

| 파일 | 변경 내용 |
|---|---|
| `docker-compose.yml` | `"6379:6379"` → `"6740:6379"` |
| `backend/src/main/resources/application-dev.yml` | `redis.port: 6379` → `redis.port: 6740` |

---

### 이슈 #2 — Android `gradle.properties` 누락

**증상**
```
Configuration `:app:debugRuntimeClasspath` contains AndroidX dependencies,
but the `android.useAndroidX` property is not enabled
```

**원인**
프로젝트 생성 시 `gradle.properties` 미생성.

**수정**
`android/gradle.properties` 신규 생성:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```

---

### 이슈 #3 — MPAndroidChart 의존성 해석 실패

**증상**
```
Could not find com.github.PhilJay:MPAndroidChart:v3.1.0.
```

**원인**
MPAndroidChart는 JitPack 전용 라이브러리로 Google/MavenCentral에 없음.
`settings.gradle`에 JitPack 저장소 미등록.

**수정**
`android/settings.gradle` `dependencyResolutionManagement.repositories`에 추가:
```groovy
maven { url 'https://jitpack.io' }
```

---

### 이슈 #4 — `AndroidManifest.xml` `xmlns:tools` 네임스페이스 누락

**증상**
```
Error parsing AndroidManifest.xml
```

**원인**
`<provider tools:node="merge">` 사용 시 `xmlns:tools` 네임스페이스 선언 필수이나 누락.

**수정**
`<manifest>` 태그에 추가:
```xml
xmlns:tools="http://schemas.android.com/tools"
```

---

### 이슈 #5 — `styles.xml` 부모 스타일 `TextAppearance.App` 미정의

**증상**
```
AAPT: error: resource style/TextAppearance.App not found.
```

**원인**
Android dot-notation 스타일(`TextAppearance.App.Title` 등)은
암묵적 부모(`TextAppearance.App`)가 리소스에 정의되어 있어야 함.

**수정**
`android/app/src/main/res/values/styles.xml`에 부모 스타일 추가:
```xml
<style name="TextAppearance.App" parent="TextAppearance.MaterialComponents.Body1" />
```

---

### 이슈 #6 — Android 기본 리소스 미생성

**증상**
```
AAPT: error: resource mipmap/ic_launcher not found.
AAPT: error: resource string/app_name not found.
AAPT: error: resource mipmap/ic_launcher_round not found.
```

**원인**
AndroidManifest.xml이 참조하는 기본 리소스가 프로젝트에 없음.

**수정 — 신규 생성 파일 목록**

| 파일 경로 | 내용 |
|---|---|
| `res/values/strings.xml` | `app_name = "WearableMonitor"` |
| `res/drawable/ic_launcher_background.xml` | `#6B5CE7` 단색 배경 shape |
| `res/drawable/ic_launcher_foreground.xml` | 흰색 원형 vector |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | adaptive-icon (background + foreground) |
| `res/mipmap-anydpi-v26/ic_launcher_round.xml` | adaptive-icon (background + foreground) |

---

### 이슈 #7 — Room `@Database(entities = [])` 빈 배열 컴파일 오류

**증상**
```
error: @Database annotation must specify list of entities
```

**원인**
Room은 `entities` 배열이 비어 있으면 컴파일을 거부함.

**수정**
TASK-10 전까지 임시로 사용할 `PlaceholderEntity` 추가:
- `data/local/entity/PlaceholderEntity.kt` 신규 생성
- `AppDatabase.kt`의 `entities = []` → `entities = [PlaceholderEntity::class]`

> **주의**: TASK-10 수행 시 실제 Entity로 교체하고 PlaceholderEntity 삭제.

---

### 이슈 #8 — `gradlew` 래퍼 스크립트 누락

**증상**
```
/usr/bin/bash: ./gradlew: No such file or directory
```

**원인**
Android 프로젝트 생성 시 Gradle Wrapper 파일 미생성.

**수정**
캐시된 Gradle 8.14 설치본을 이용해 래퍼 생성:
```bash
gradle wrapper --gradle-version=8.9
```
AGP 8.3.2 호환 버전인 Gradle 8.9로 설정.

---

## 5. 최종 검증 결과

### Back-end
```
./gradlew build  →  BUILD SUCCESSFUL
```

### Front-end
```
npm run dev  →  VITE ready on http://localhost:5173/
```

### 인프라 (Docker)
```
wearable-postgres  timescale/timescaledb:latest-pg16  Up (healthy)  0.0.0.0:5432->5432/tcp
wearable-redis     redis:7-alpine                     Up (healthy)  0.0.0.0:6740->6379/tcp
```
```
pg_isready -U wearable -d wearable_db  →  /var/run/postgresql:5432 - accepting connections
redis-cli ping  →  PONG
```

### Android
```
./gradlew assembleDebug  →  BUILD SUCCESSFUL in 52s
```

### global.css 주요 변수 선언 확인

| CSS 변수 | 선언값 | 가이드 기준 |
|---|---|---|
| `--primary` | `#6b5ce7` | `#6B5CE7` ✅ |
| `--primary-dark` | `#5647cc` | `#5647CC` ✅ |
| `--ok` | `#27ae60` | `#27AE60` ✅ |
| `--warn` | `#e67e22` | `#E67E22` ✅ |
| `--danger` | `#e74c3c` | `#E74C3C` ✅ |
| `--text` | `#1c2333` | `#1C2333` ✅ |
| `--text-sub` | `#6c7a89` | `#6C7A89` ✅ |
| `--gray-border` | `#e0e4ea` | `#E0E4EA` ✅ |
| `--gray-light` | `#f0f2f5` | `#F0F2F5` ✅ |

### colors.xml 주요 색상 확인

| 리소스명 | 선언값 | 가이드 기준 |
|---|---|---|
| `@color/primary` | `#6B5CE7` | ✅ |
| `@color/ok` | `#27AE60` | ✅ |
| `@color/warn` | `#E67E22` | ✅ |
| `@color/danger` | `#E74C3C` | ✅ |
| `@color/text_primary` | `#1C2333` | ✅ |
| `@color/text_secondary` | `#6C7A89` | ✅ |
| `@color/border` | `#E0E4EA` | ✅ |
| `@color/bg_page` | `#F0F2F5` | ✅ |

---

## 6. 후속 TASK 진행 시 유의사항

| 항목 | 내용 |
|---|---|
| Redis 포트 | 호스트 포트 **6740** 사용 (컨테이너 내부 6379) |
| Android `PlaceholderEntity` | TASK-10에서 실제 Entity 추가 후 **삭제 필요** |
| Android Gradle 버전 | Gradle **8.9** / AGP **8.3.2** |
| Back-end 실행 프로파일 | `--spring.profiles.active=dev` 필수 |

---

## 7. 산출물 목록

### 신규 생성 파일 (검증 과정에서 추가)

| 파일 경로 | 용도 |
|---|---|
| `android/gradle.properties` | AndroidX 활성화, Gradle JVM 옵션 |
| `android/gradlew`, `gradlew.bat` | Gradle Wrapper 실행 스크립트 |
| `android/gradle/wrapper/gradle-wrapper.*` | Gradle Wrapper 설정 (8.9) |
| `android/app/src/main/res/values/strings.xml` | 앱 이름 문자열 리소스 |
| `android/app/src/main/res/drawable/ic_launcher_background.xml` | 런처 아이콘 배경 |
| `android/app/src/main/res/drawable/ic_launcher_foreground.xml` | 런처 아이콘 전경 |
| `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive 런처 아이콘 |
| `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | Adaptive 원형 런처 아이콘 |
| `android/app/src/main/java/.../data/local/entity/PlaceholderEntity.kt` | Room 임시 Entity (TASK-10 교체) |

### 수정 파일 (검증 과정에서 변경)

| 파일 경로 | 변경 내용 |
|---|---|
| `docker-compose.yml` | Redis 호스트 포트 6379 → 6740 |
| `backend/.../application-dev.yml` | Redis port 6379 → 6740 |
| `android/settings.gradle` | JitPack 저장소 추가 |
| `android/app/src/main/AndroidManifest.xml` | `xmlns:tools` 네임스페이스 추가 |
| `android/app/src/main/res/values/styles.xml` | `TextAppearance.App` 부모 스타일 추가 |
| `android/app/src/main/java/.../data/local/AppDatabase.kt` | entities에 PlaceholderEntity 등록 |
