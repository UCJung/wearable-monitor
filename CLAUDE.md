# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# 웨어러블 환자 모니터링 시스템 MVP — 개발 마스터 가이드

> Claude Code가 이 프로젝트에서 작업할 때 **반드시 이 파일을 먼저 읽고 시작**한다.
> 모든 판단 기준, 컨벤션, 작업 프로세스는 이 파일이 최우선이다.

---

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 프로젝트명 | 웨어러블 기반 환자 모니터링 시스템 MVP |
| 실증 규모 | 50명 환자 |
| 수집 기기 | Samsung Galaxy Watch 7 (Health Connect API) |
| 핵심 기능 | ① 환자·기기 할당 관리  ② 생체신호 자동 수집·저장  ③ 조회·엑셀 다운로드 |
| 수집 항목 | 14종 (생체신호 7 / 활동 3 / 수면 3 / AI 종합 1) |

---

## 2. 참조 문서 위치

```
docs/
├── 웨어러블_환자모니터링_MVP_설계서.docx     ← 시스템 설계 + DB 테이블 정의
├── 웨어러블_환자모니터링_기능명세서.docx      ← FNT-001~010 기능 명세
├── STYLE_GUIDE_WEB.md                       ← React 스타일 가이드 (색상·컴포넌트)
├── STYLE_GUIDE_ANDROID.md                   ← Android 스타일 가이드 (colors·dimens)
├── ui_web_monitoring.html                   ← 웹 UI 확정 시안
└── ui_app_screens.html                      ← 앱 UI 확정 시안

tasks/
└── TASK-00.md ~ TASK-11.md                  ← TASK별 상세 체크리스트 + 프롬프트
```

---

## 3. 작업 프로세스 (모든 TASK 공통 필수)

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  1. 계획수립  │───▶│  2. 계획검토  │───▶│  3. 계획승인  │───▶│  4. 작업수행  │───▶│  5. 검증실행  │───▶│  6. 결과보고  │
│  Claude 작성  │    │  담당자 확인  │    │  담당자 승인  │    │  Claude 실행  │    │  자동 실행    │    │  보고서 생성  │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                                                                                        │ 오류 시
                                                                                        ▼
                                                                                  수정 후 재실행
```

### 프로세스 규칙
- Claude는 각 TASK 시작 시 **계획서를 먼저 출력하고 대기**한다
- 담당자가 **"TASK-XX 승인"** 을 입력해야만 코드 작성을 시작한다
- 승인 전 코드 작성 절대 금지
- 작업 중 불명확한 사항은 즉시 질문하고 확인 후 진행한다
- 각 산출물 완료 시 해당 TASK 체크리스트에 즉시 체크한다
- **작업수행 완료 후 해당 TASK MD의 "Step 3 — 완료 검증" 명령어를 자동으로 실행한다**
- 검증 실패 시 원인을 분석·수정하고 해당 검증 항목을 재실행한다
- **모든 검증 항목 통과 후 `tasks/TASK-XX-수행결과.md`를 자동으로 생성한다** (섹션 13 템플릿 준수)
- 자동화 불가 UI 검증(브라우저 렌더링, 에뮬레이터 동작, 색상 육안 확인 등)은 수행결과 보고서 섹션 5에 **"수동 확인 필요"** 항목으로 명시한다

---

## 4. 전체 개발 Phase

| Phase | TASK | 내용 | 선행 |
|---|---|---|---|
| 0 | TASK-00 | 환경 설정 및 프로젝트 초기화 | 없음 |
| 1 | TASK-01 | DB 설계 및 초기화 (DDL + 마스터 데이터) | TASK-00 |
| 2 | TASK-02 | Back-end: 인증 + 환자·장치 관리 API | TASK-01 |
| 3 | TASK-03 | Back-end: 수집·조회·모니터링·엑셀 API | TASK-02 |
| 4 | TASK-04 | Front-end: 초기화 + 공통 컴포넌트 | TASK-00 |
| 5 | TASK-05 | Front-end: 기준데이터 관리 화면 | TASK-04, TASK-02 |
| 6 | TASK-06 | Front-end: 현황 모니터링 화면 | TASK-05, TASK-03 |
| 7 | TASK-07 | Front-end: 이력 관리 + 엑셀 다운로드 | TASK-06 |
| 8 | TASK-08 | Android: 초기화 + Health Connect | TASK-00 |
| 9 | TASK-09 | Android: 인증 + 설정 Wizard | TASK-08, TASK-02 |
| 10 | TASK-10 | Android: 수집 Worker + 현황 대시보드 | TASK-09, TASK-03 |
| 11 | TASK-11 | 통합 테스트 및 배포 | TASK-07, TASK-10 |

---

## 5. 기술 스택

### Back-end
```
Java 17 / Spring Boot 3.x / Gradle (Groovy)
Spring Security + JWT (Access 1h / Refresh 7d, Redis 저장)
JPA + QueryDSL 5.x
PostgreSQL 16 + TimescaleDB
Redis 7
Apache POI 5.3.x (엑셀)
JUnit 5 + Mockito + Testcontainers
```

### Front-end
```
React 18 + TypeScript 5 / Vite 5
Tailwind CSS 3 + shadcn/ui
Zustand 4 / TanStack Query v5 / React Router v6
Axios (JWT 인터셉터) / Recharts / react-hook-form + zod / date-fns
```

### Android
```
Kotlin 1.9+ / MVVM + Clean Architecture
Hilt / Coroutines + Flow / Retrofit2 + OkHttp3
Room / DataStore / WorkManager
Health Connect API (androidx.health.connect)
MPAndroidChart / EncryptedSharedPreferences
minSdk 28 (Android 9) / targetSdk 34
```

---

## 6. 빌드 및 실행 명령어

### 인프라 (Docker)
```bash
docker-compose up -d              # PostgreSQL 16 + TimescaleDB + Redis 7 기동
docker-compose down               # 중지
```

### Back-end
```bash
cd backend
./gradlew build                   # 전체 빌드 + 테스트
./gradlew bootRun --args='--spring.profiles.active=dev'   # 개발 서버 실행
./gradlew test                    # 전체 테스트
./gradlew test --tests "*.PatientServiceTest"             # 단일 테스트 클래스
./gradlew compileJava             # QueryDSL Q클래스 생성 확인
```

### Front-end
```bash
cd frontend
npm install
npm run dev                       # 개발 서버 (localhost:5173)
npm run build                     # 프로덕션 빌드
npm run lint                      # ESLint
```

### Android
```bash
cd android
./gradlew assembleDebug           # 디버그 APK 빌드
./gradlew test                    # 단위 테스트
./gradlew connectedAndroidTest    # 기기/에뮬레이터 테스트
```

### DB 상태 확인
```sql
-- TimescaleDB 하이퍼테이블 확인
SELECT * FROM timescaledb_information.hypertables;
-- 수집 항목 마스터 확인
SELECT item_code, item_name_ko FROM collection_item_definitions ORDER BY display_order;
```

---

## 7. 디렉터리 구조

```
wearable-monitor/
├── CLAUDE.md
├── docs/                             ← 설계 문서 + TASK MD 파일 (TASK-00~11)
├── docker-compose.yml
│
├── backend/
│   └── src/main/java/com/wearable/monitor/
│       ├── common/                   ← ApiResponse, ErrorCode, WearableException
│       ├── config/                   ← Security, JWT, QueryDSL, Redis
│       ├── domain/
│       │   ├── patient/
│       │   ├── device/
│       │   ├── assignment/
│       │   ├── biometric/
│       │   └── itemdef/
│       └── api/
│           ├── auth/
│           ├── patient/
│           ├── device/
│           ├── assignment/
│           ├── biometric/
│           ├── monitoring/
│           └── export/
│
├── frontend/
│   └── src/
│       ├── api/                      ← axiosInstance + API 함수별 파일
│       ├── components/
│       │   ├── layout/               ← Layout, Sidebar, Header
│       │   └── ui/                   ← Button, Badge, Table, Modal, Toast
│       ├── pages/
│       │   ├── patients/
│       │   ├── devices/
│       │   ├── monitoring/
│       │   └── history/
│       ├── stores/                   ← authStore, toastStore
│       ├── hooks/
│       ├── types/
│       └── styles/global.css         ← CSS 변수 전체 선언
│
└── android/app/src/main/
    ├── java/com/wearable/monitor/
    │   ├── ui/                       ← login, setup, dashboard, history
    │   ├── data/                     ← local(Room), remote(Retrofit), repository
    │   ├── domain/                   ← model, usecase
    │   ├── health/                   ← HealthConnectManager
    │   ├── worker/                   ← SyncWorker
    │   └── di/                       ← NetworkModule, DatabaseModule
    └── res/values/
        ├── colors.xml
        ├── dimens.xml
        └── styles.xml
```

---

## 8. DB 핵심 규칙

### 테이블 5종
| 테이블 | 핵심 규칙 |
|---|---|
| patients | patient_code 자동 채번 (PT-NNNN), status 소프트 삭제 |
| devices | serial_number UNIQUE, device_status 상태 관리 |
| patient_device_assignments | 환자당 ACTIVE 할당 1건만 허용 |
| collection_item_definitions | 14건 고정 마스터 데이터 |
| patient_biometric_history | TimescaleDB 하이퍼테이블, measured_at 파티션 키 |

### 소프트 삭제 원칙
- `DELETE` SQL 절대 사용 금지
- patients: `status = 'DELETED'`
- devices: `device_status = 'RETIRED'`

---

## 9. API 공통 규칙

### 응답 형식
```json
{ "code": "SUCCESS", "message": "처리되었습니다.", "data": { ... } }
{ "code": "PATIENT_NOT_FOUND", "message": "환자를 찾을 수 없습니다.", "data": null }

```

### URL 규칙
```
GET/POST  /api/v1/{resource}
GET/PUT/DELETE  /api/v1/{resource}/{id}
PUT  /api/v1/{resource}/{id}/{action}   예) /api/v1/assignments/{id}/return
```

---

## 10. 코딩 컨벤션

### Back-end
- 응답: `ApiResponse<T>` 래퍼 항상 사용
- 예외: `WearableException(ErrorCode)` 사용
- 트랜잭션: Service 메서드에 `@Transactional` 명시
- 로그: `@Slf4j` + `log.info/warn/error`

### Front-end
- 색상: CSS 변수 `var(--primary)` 사용 (HEX 하드코딩 금지)
- 타입: `I` 접두사 금지, PascalCase 사용

### Android
- 색상: `@color/` 참조 (하드코딩 금지)
- 크기: `@dimen/` 참조 (dp 하드코딩 금지)
- 민감 정보: `EncryptedSharedPreferences` 저장

---

## 11. 색상 시스템 (웹·앱 공통 기준)

| 역할 | HEX | Web CSS 변수 | Android |
|---|---|---|---|
| Primary | `#6B5CE7` | `--primary` | `@color/primary` |
| Primary Dark | `#5647CC` | `--primary-dark` | `@color/primary_dark` |
| OK 정상 | `#27AE60` | `--ok` | `@color/ok` |
| Warn 경고 | `#E67E22` | `--warn` | `@color/warn` |
| Danger 위험 | `#E74C3C` | `--danger` | `@color/danger` |
| Text | `#1C2333` | `--text` | `@color/text_primary` |
| Text Sub | `#6C7A89` | `--text-sub` | `@color/text_secondary` |
| Border | `#E0E4EA` | `--gray-border` | `@color/border` |
| Page BG | `#F0F2F5` | `--gray-light` | `@color/bg_page` |

---

## 12. 완료 기준 (Definition of Done)

각 TASK는 아래를 **모두 만족**해야 완료로 인정한다.

- [ ] TASK MD 체크리스트 전 항목 완료
- [ ] 기능 명세서 요구사항 100% 구현
- [ ] 스타일 가이드 색상·크기 하드코딩 없음
- [ ] Back-end: 단위 테스트 작성 및 통과
- [ ] 빌드 오류 0건
- [ ] 주요 예외 케이스 처리 확인
- [ ] `tasks/TASK-XX-수행결과.md` 생성 완료

---

## 13. 수행결과 보고서 규칙

### 생성 조건
- TASK MD **"Step 3 — 완료 검증"의 자동화 가능한 모든 항목이 통과**된 후 생성한다
- 수동 확인 항목(브라우저 UI, 에뮬레이터 동작 등)은 보고서 **섹션 5에 "수동 확인 필요"로 별도 표기**한다

### 파일 경로
```
tasks/TASK-XX-수행결과.md   (XX = 두 자리 TASK 번호, 예: TASK-01-수행결과.md)
```

### 필수 섹션 구성

```markdown
# TASK-XX 수행 결과 보고서

> 작업일: YYYY-MM-DD
> 작업자: Claude Code (claude-sonnet-4-6)
> 상태: **완료**

---

## 1. 작업 개요
(해당 TASK의 목적 1~2줄 요약)

---

## 2. 완료 기준 달성 현황
(TASK MD의 완료 기준 항목별 ✅ / ❌ 표)

---

## 3. 체크리스트 완료 현황
(TASK MD의 체크리스트 항목을 소분류별 표로 정리)

---

## 4. 발견 이슈 및 수정 내역
(작업 중 발생한 오류·수정 사항을 이슈별로 기술)
(이슈가 없으면 "발견된 이슈 없음" 으로 기재)

각 이슈는 아래 형식으로 작성한다:
### 이슈 #N — 이슈 제목
**증상**: 오류 메시지 또는 현상
**원인**: 원인 분석
**수정**: 수정 파일 및 변경 내용

---

## 5. 최종 검증 결과
(빌드 로그, 테스트 결과, 실행 확인 내용 기재)

---

## 6. 후속 TASK 유의사항
(다음 TASK 진행 시 알아야 할 사항, 없으면 생략)

---

## 7. 산출물 목록
(신규 생성 파일 / 수정 파일 전체 목록을 표로 기재)
```

### 작성 원칙
- 이슈가 없더라도 섹션 4는 **반드시 포함**한다 ("발견된 이슈 없음" 기재)
- 빌드/테스트 결과는 실제 출력 로그를 코드 블록으로 첨부한다
- 수정 파일이 없으면 섹션 7의 "수정 파일" 표를 생략한다
