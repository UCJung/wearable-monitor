# 웨어러블 기반 환자 모니터링 시스템 MVP

Samsung Galaxy Watch 7(Health Connect API)로 수집한 **14종 생체신호**를
중앙 서버에 자동 저장하고 웹 화면에서 조회·관리하는 시스템입니다.

- **실증 규모**: 50명 환자
- **수집 기기**: Samsung Galaxy Watch 7

### 핵심 기능

1. **환자·기기 할당 관리** — 환자 등록, 워치 등록, 1:1 할당/반납
2. **생체신호 자동 수집·저장** — 워치 앱이 30분 주기로 배치 업로드
3. **조회·엑셀 다운로드** — 실시간 모니터링 대시보드 + 이력 엑셀 내보내기

### 수집 항목 (14종)

| 분류 | 항목 |
|---|---|
| 생체신호 (7) | 심박수, 심박변이도(HRV), 혈중산소(SpO2), 피부온도, 호흡수, 스트레스, 혈압 |
| 활동 (3) | 걸음수, 활동 칼로리, 운동 시간 |
| 수면 (3) | 수면 시간, 수면 단계, 수면 중 SpO2 |
| AI 종합 (1) | 에너지 스코어 |

---

## 기술 스택

### Back-end
| 기술 | 버전 |
|---|---|
| Java | 17 |
| Spring Boot | 3.x |
| Spring Security + JWT | Access 1h / Refresh 7d (Redis 저장) |
| JPA + QueryDSL | 5.x |
| PostgreSQL + TimescaleDB | 16 |
| Redis | 7 |
| Apache POI (엑셀) | 5.3.x |
| 테스트 | JUnit 5 + Mockito + Testcontainers |

### Front-end
| 기술 | 버전 |
|---|---|
| React + TypeScript | 18 / 5 |
| Vite | 5 |
| Tailwind CSS + shadcn/ui | 3 |
| Zustand | 4 |
| TanStack Query | v5 |
| React Router | v6 |
| Recharts | — |
| react-hook-form + zod | — |

### Android
| 기술 | 버전 |
|---|---|
| Kotlin | 1.9+ |
| 아키텍처 | MVVM + Clean Architecture |
| DI | Hilt |
| 비동기 | Coroutines + Flow |
| 네트워크 | Retrofit2 + OkHttp3 |
| 로컬 DB | Room |
| 백그라운드 | WorkManager |
| Health Connect | androidx.health.connect 1.1.0-alpha10 |
| minSdk / targetSdk | 28 (Android 9) / 34 |

---

## 아키텍처

```
┌────────────┐    ┌─────────────────┐    ┌──────────────┐
│  Frontend   │───▶│   Nginx (:80)   │───▶│ Backend(:8080)│
│  (React)    │    │ SPA + API Proxy │    │ Spring Boot   │
└────────────┘    └─────────────────┘    └──────┬───────┘
                                                │
                               ┌────────────────┼────────────────┐
                               │                │                │
                        ┌──────▼──────┐  ┌──────▼──────┐  ┌─────▼─────┐
                        │ PostgreSQL  │  │    Redis    │  │  Android  │
                        │ + TimescaleDB│  │  (JWT 저장) │  │   Watch   │
                        │   (:5432)   │  │   (:6379)   │  │    App    │
                        └─────────────┘  └─────────────┘  └───────────┘
```

---

## 실행 방법

### 1. 인프라 기동
```bash
docker-compose up -d
```

### 2. Back-end 실행
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```
- API 서버: http://localhost:8080
- 초기 계정: admin / admin1234

### 3. Front-end 실행
```bash
cd frontend
npm install
npm run dev
```
- 웹 화면: http://localhost:5173

### 4. Android 빌드
```bash
cd android
./gradlew assembleDebug
```

---

## 프로덕션 배포

### Docker Compose (원스텝)
```bash
cp .env.example .env
# .env 파일에서 DB_PASSWORD, JWT_SECRET 등을 수정하세요
docker-compose -f docker-compose.prod.yml up -d --build
```

---

## 테스트

### Back-end
```bash
cd backend
./gradlew test                                           # 전체 (단위 + 통합)
./gradlew test --tests "*.integration.*"                 # 통합 테스트만
./gradlew test --tests "*.BiometricServiceTest"          # 단일 클래스
```
> 통합 테스트는 Testcontainers로 PostgreSQL + Redis를 자동 기동합니다. Docker가 실행 중이어야 합니다.

### Front-end
```bash
cd frontend
npm run lint
npm run build
```

### Android
```bash
cd android
./gradlew test                    # 단위 테스트
./gradlew connectedAndroidTest    # 기기/에뮬레이터 테스트
```

---

## 디렉터리 구조

```
wearable-monitor/
├── CLAUDE.md                          개발 가이드 (Claude Code용)
├── docker-compose.yml                 개발 환경 (DB + Redis)
├── docker-compose.prod.yml            프로덕션 배포 (전체 서비스)
├── .env.example                       환경 변수 템플릿
├── docs/                              설계 문서, 스타일 가이드, TASK MD
│
├── backend/
│   └── src/main/java/com/wearable/monitor/
│       ├── common/                    ApiResponse, ErrorCode, WearableException
│       ├── config/                    Security, JWT, QueryDSL, Redis
│       ├── domain/
│       │   ├── patient/               환자 엔티티·리포지토리
│       │   ├── device/                기기 엔티티·리포지토리
│       │   ├── assignment/            환자-기기 할당 엔티티·리포지토리
│       │   ├── biometric/             생체신호 이력 (TimescaleDB)
│       │   └── itemdef/               수집 항목 정의 마스터
│       └── api/
│           ├── auth/                  인증 (JWT 로그인·갱신·로그아웃)
│           ├── patient/               환자 CRUD
│           ├── device/                기기 CRUD
│           ├── assignment/            할당·반납
│           ├── biometric/             배치 업로드·이력 조회
│           ├── monitoring/            현황 대시보드·일별 요약
│           └── export/                엑셀 다운로드
│
├── frontend/
│   └── src/
│       ├── api/                       axiosInstance + API 함수별 파일
│       ├── components/
│       │   ├── layout/                Layout, Sidebar, Header
│       │   └── ui/                    Button, Badge, Table, Modal, Toast
│       ├── pages/
│       │   ├── patients/              환자 관리 화면
│       │   ├── devices/               기기 관리 화면
│       │   ├── monitoring/            현황 모니터링 대시보드
│       │   └── history/               이력 관리 + 엑셀 다운로드
│       ├── stores/                    authStore, toastStore (Zustand)
│       ├── hooks/                     커스텀 훅
│       ├── types/                     TypeScript 타입 정의
│       └── styles/global.css          CSS 변수 전체 선언
│
└── android/app/src/main/
    ├── java/com/wearable/monitor/
    │   ├── ui/                        login, setup, dashboard, history
    │   ├── data/
    │   │   ├── local/                 Room DB (BiometricEntity, SyncStatusEntity)
    │   │   ├── remote/                Retrofit API 서비스
    │   │   └── repository/            데이터 저장소
    │   ├── domain/                    model, usecase
    │   ├── health/                    HealthConnectManager
    │   ├── worker/                    SyncWorker (30분 주기 배치 업로드)
    │   └── di/                        NetworkModule, DatabaseModule (Hilt)
    └── res/values/
        ├── colors.xml
        ├── dimens.xml
        └── styles.xml
```

---

## API 엔드포인트

총 7개 컨트롤러, 23개 엔드포인트.

### 인증 (`/api/v1/auth`)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/auth/login` | 로그인 (Access + Refresh 토큰 반환) |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/logout` | 로그아웃 (Redis 토큰 무효화) |

### 환자 (`/api/v1/patients`)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/patients` | 환자 등록 (patient_code 자동 채번) |
| GET | `/api/v1/patients` | 환자 목록 조회 (페이징, 필터) |
| GET | `/api/v1/patients/{id}` | 환자 상세 조회 |
| PUT | `/api/v1/patients/{id}` | 환자 정보 수정 |
| DELETE | `/api/v1/patients/{id}` | 환자 소프트 삭제 |

### 기기 (`/api/v1/devices`)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/devices` | 기기 등록 |
| GET | `/api/v1/devices` | 기기 목록 조회 (페이징) |
| GET | `/api/v1/devices/{id}` | 기기 상세 조회 |
| PUT | `/api/v1/devices/{id}` | 기기 정보 수정 |
| DELETE | `/api/v1/devices/{id}` | 기기 소프트 삭제 |

### 할당 (`/api/v1/assignments`)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/assignments` | 환자-기기 할당 (ACTIVE 1건 제한) |
| GET | `/api/v1/assignments` | 할당 이력 조회 (페이징) |
| GET | `/api/v1/assignments/{id}` | 할당 상세 조회 |
| PUT | `/api/v1/assignments/{id}/return` | 기기 반납 처리 |

### 생체신호 (`/api/v1/biometric`)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/biometric/batch` | 배치 업로드 (최대 500건) |
| GET | `/api/v1/biometric/{patientId}` | 환자별 이력 조회 (기간·항목 필터) |

### 모니터링 (`/api/v1/monitoring`)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/v1/monitoring/assignment-status` | 환자·기기 할당 현황 요약 |
| GET | `/api/v1/monitoring/daily-summary` | 일별 수집 데이터 요약 |

### 엑셀 내보내기 (`/api/v1/export`)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/v1/export/patient/{patientId}` | 환자별 수집 이력 Excel 다운로드 |
| GET | `/api/v1/export/all` | 전체 수집 이력 Excel 다운로드 |

---

## DB 스키마

5개 테이블 구성. `patient_biometric_history`는 TimescaleDB 하이퍼테이블로 시계열 최적화.

| 테이블 | 설명 | 비고 |
|---|---|---|
| `patients` | 환자 정보 | patient_code 자동 채번 (PT-NNNN), 소프트 삭제 |
| `devices` | 기기 정보 | serial_number UNIQUE, 소프트 삭제 |
| `patient_device_assignments` | 환자-기기 할당 이력 | 환자당 ACTIVE 할당 1건만 허용 |
| `collection_item_definitions` | 수집 항목 정의 마스터 | 14건 고정 데이터 |
| `patient_biometric_history` | 생체신호 수집 이력 | TimescaleDB 하이퍼테이블 (measured_at 파티션) |

### 주요 규칙
- **소프트 삭제**: `DELETE` SQL 미사용. 환자는 `status='DELETED'`, 기기는 `device_status='RETIRED'`
- **시계열 저장**: `patient_biometric_history`는 TimescaleDB 하이퍼테이블로 자동 파티셔닝

---

## 환경 변수

프로덕션 배포 시 `.env.example`을 복사하여 `.env`로 수정하세요.

| 변수 | 설명 | 기본값 |
|---|---|---|
| DB_NAME | PostgreSQL DB명 | wearable_db |
| DB_USER | DB 사용자 | wearable |
| DB_PASSWORD | DB 비밀번호 | (변경 필수) |
| JWT_SECRET | JWT 서명 키 (256bit 이상) | (변경 필수) |
| JWT_ACCESS_TOKEN_EXPIRY | Access Token 유효시간 (ms) | 3600000 (1시간) |
| JWT_REFRESH_TOKEN_EXPIRY | Refresh Token 유효시간 (ms) | 604800000 (7일) |

개발 환경 설정: `backend/src/main/resources/application-dev.yml`

---

## 개발 완료 현황

전체 12개 TASK 완료.

| Phase | TASK | 내용 | 상태 |
|---|---|---|---|
| 0 | TASK-00 | 환경 설정 및 프로젝트 초기화 | 완료 |
| 1 | TASK-01 | DB 설계 및 초기화 (DDL + 마스터 데이터) | 완료 |
| 2 | TASK-02 | Back-end: 인증 + 환자·장치 관리 API | 완료 |
| 3 | TASK-03 | Back-end: 수집·조회·모니터링·엑셀 API | 완료 |
| 4 | TASK-04 | Front-end: 초기화 + 공통 컴포넌트 | 완료 |
| 5 | TASK-05 | Front-end: 기준데이터 관리 화면 | 완료 |
| 6 | TASK-06 | Front-end: 현황 모니터링 화면 | 완료 |
| 7 | TASK-07 | Front-end: 이력 관리 + 엑셀 다운로드 | 완료 |
| 8 | TASK-08 | Android: 초기화 + Health Connect | 완료 |
| 9 | TASK-09 | Android: 인증 + 설정 Wizard | 완료 |
| 10 | TASK-10 | Android: 수집 Worker + 현황 대시보드 | 완료 |
| 11 | TASK-11 | 통합 테스트 및 배포 | 완료 |
