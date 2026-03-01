# TASK-00 — 환경 설정 및 프로젝트 초기화

> 선행 없음 | 후속: TASK-01, TASK-04, TASK-08

---

## 작업 목표
모노레포 구조 생성, Back-end/Front-end/Android 각 파트의 기본 뼈대와
공통 설정(색상·응답·예외·JWT)을 구성한다.

---

## 체크리스트

### 0-1. 모노레포 구조
- [x] `wearable-monitor/` 루트 생성
- [x] `docs/` 설계 문서 배치
- [x] `tasks/` TASK MD 파일 배치
- [x] `.gitignore` (Java + Node + Android + IDE + `.env`)
- [x] 루트 `README.md` (프로젝트 설명 + 실행 방법)
- [x] `docker-compose.yml` (PostgreSQL 16 + TimescaleDB + Redis 7)

### 0-2. Back-end 초기화
- [x] `backend/build.gradle` 의존성 정의
  ```
  spring-boot-starter-web, security, data-jpa, data-redis
  postgresql, querydsl-jpa, jjwt, poi-ooxml, lombok
  spring-boot-starter-test, testcontainers
  ```
- [x] `application.yml` + `application-dev.yml` (DB/Redis/JWT 설정)
- [x] `ApiResponse<T>` 공통 응답 래퍼
- [x] `ErrorCode` enum (공통 오류 코드)
- [x] `WearableException` + `GlobalExceptionHandler`
- [x] `JwtProvider` (토큰 생성·검증·만료 처리)
- [x] `SecurityConfig` (공개 엔드포인트, CORS 설정)
- [x] `JpaAuditingConfig` (@EnableJpaAuditing)
- [x] `QueryDslConfig` (JPAQueryFactory Bean)
- [x] `RedisConfig` (RedisTemplate Bean)

### 0-3. Front-end 초기화
- [x] `npm create vite@latest frontend -- --template react-ts`
- [x] 패키지 설치
  ```
  tailwindcss @tailwindcss/forms
  zustand @tanstack/react-query
  react-router-dom axios
  recharts react-hook-form zod @hookform/resolvers
  date-fns shadcn/ui 초기화
  ```
- [x] `tailwind.config.js` CSS 변수 연동
- [x] `src/styles/global.css` CSS 변수 전체 선언
  (STYLE_GUIDE_WEB.md 섹션 14 기준)
- [x] `src/api/axiosInstance.ts` JWT 인터셉터 (자동 첨부 + 401 재발급)
- [x] `src/stores/authStore.ts` Zustand 인증 스토어
- [x] `src/stores/toastStore.ts` 토스트 알림 스토어
- [x] `src/components/layout/Layout.tsx` (Sidebar + Header + Outlet)
- [x] `src/components/layout/Sidebar.tsx` (메뉴 트리, 활성 상태)
- [x] `src/components/layout/Header.tsx` (브레드크럼 + 사용자 정보)
- [x] `src/router/index.tsx` (기본 라우팅 구조)
- [x] `src/types/common.ts` (ApiResponse, PageResponse 타입)

### 0-4. Android 초기화
- [x] Android 프로젝트 생성 (minSdk 28, Kotlin)
- [x] `build.gradle (app)` 의존성 정의
  ```
  hilt-android, lifecycle-viewmodel-ktx
  kotlinx-coroutines-android
  retrofit2, okhttp3, okhttp-logging-interceptor
  room-runtime, room-ktx, room-compiler
  datastore-preferences, work-runtime-ktx
  health-connect-client
  mpandroidchart
  security-crypto (EncryptedSharedPreferences)
  ```
- [x] `res/values/colors.xml` (STYLE_GUIDE_ANDROID.md 섹션 14)
- [x] `res/values/dimens.xml` (STYLE_GUIDE_ANDROID.md 섹션 15)
- [x] `res/values/styles.xml` (TextAppearance 정의)
- [x] `AndroidManifest.xml` Health Connect 권한 6종 선언
- [x] `WearableApplication.kt` (@HiltAndroidApp)
- [x] `di/NetworkModule.kt` (Retrofit + OkHttp DI)
- [x] `di/DatabaseModule.kt` (Room DI)

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-00.md를 읽어줘.

읽은 후 아래 형식으로 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-00

### 생성할 파일 목록
(경로 | 용도 | 비고)

### 작업 순서 및 예상 시간

### 결정이 필요한 사항
(불명확하거나 선택지가 있는 항목)

### 주의사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-00 승인.

아래 순서로 작업을 시작해.

1. 모노레포 디렉터리 구조 + .gitignore + README.md
2. docker-compose.yml (PostgreSQL + TimescaleDB + Redis)
3. Back-end 초기화
   - build.gradle 의존성
   - application.yml / application-dev.yml
   - 공통 클래스: ApiResponse, ErrorCode, WearableException,
     GlobalExceptionHandler, JwtProvider, SecurityConfig
4. Front-end 초기화
   - Vite 프로젝트 생성
   - global.css CSS 변수 (STYLE_GUIDE_WEB.md 섹션 14 기준)
   - Layout, Sidebar, Header 컴포넌트
   - axiosInstance (JWT 인터셉터)
   - authStore, toastStore
5. Android 초기화
   - build.gradle 의존성
   - colors.xml, dimens.xml, styles.xml (스타일 가이드 기준)
   - AndroidManifest.xml Health Connect 권한
   - Hilt 설정

각 항목 완료 시 체크리스트에 체크하고 계속 진행해.
```

### Step 3 — 완료 검증
```
TASK-00 완료 검증을 해줘.

1. `cd backend && ./gradlew build` 실행하고 결과 알려줘
2. `cd frontend && npm run dev` 실행하고 localhost:5173 접속 확인
3. `docker-compose up -d` 실행하고 PostgreSQL, Redis 상태 확인
4. Android build 오류 여부 확인
5. global.css에 --primary 변수가 #6B5CE7로 선언됐는지 확인

오류가 있으면 수정하고 다시 알려줘.
```

---

## 완료 기준
- [x] `./gradlew build` 성공
- [x] `npm run dev` 정상 구동 (localhost:5173)
- [x] `docker-compose up` PostgreSQL + Redis 정상 기동
- [x] Android 빌드 오류 0건
- [x] `global.css` CSS 변수 전체 선언 확인
- [x] `colors.xml`, `dimens.xml` 스타일 가이드 기준 적용
