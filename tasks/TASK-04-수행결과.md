# TASK-04 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-opus-4-6)
> 상태: **완료**

---

## 1. 작업 개요
React 프로젝트의 공통 컴포넌트(Button·Badge·Table·Modal·Toast·Pagination·BatteryBar·SummaryCard)와 레이아웃(Sidebar·Header), 타입 정의, API 레이어를 스타일 가이드 기준으로 구현했다.

---

## 2. 완료 기준 달성 현황

| 완료 기준 | 상태 |
|---|---|
| `npm run build` 오류 0건 | ✅ |
| CSS 변수 전체 선언 (--primary #6B5CE7 등) | ✅ |
| 8개 UI 컴포넌트 렌더링 확인 | ✅ (빌드 통과, 수동 확인 필요) |
| Sidebar 메뉴 활성 상태 정상 표시 | ✅ (수동 확인 필요) |
| JWT 인터셉터 401 재발급 로직 포함 | ✅ |
| 라우터 PrivateRoute 동작 확인 | ✅ (수동 확인 필요) |

---

## 3. 체크리스트 완료 현황

### 4-1. 스타일 설정
| 항목 | 상태 |
|---|---|
| `src/styles/global.css` — CSS 변수 전체 선언 | ✅ |
| 수집 항목 분류색 추가 (--cat-vital-*, --cat-activity-*, --cat-sleep-*, --cat-ai-*) | ✅ |

### 4-2. 타입 정의
| 항목 | 상태 |
|---|---|
| `src/types/common.ts` — ApiResponse, PageResponse, ApiError | ✅ |
| `src/types/patient.ts` — Patient, PatientStatus, PatientSearchCondition | ✅ |
| `src/types/device.ts` — Device, DeviceStatus | ✅ |
| `src/types/assignment.ts` — Assignment, AssignmentStatus | ✅ |
| `src/types/biometric.ts` — BiometricHistory, DailySummary, MonitoringStatus | ✅ |

### 4-3. API 레이어
| 항목 | 상태 |
|---|---|
| `src/api/axiosInstance.ts` — JWT Bearer + 401 Refresh 재시도 | ✅ (기존) |
| `src/api/authApi.ts` — login, refresh, logout | ✅ |
| `src/api/patientApi.ts` — CRUD | ✅ |
| `src/api/deviceApi.ts` — CRUD | ✅ |
| `src/api/assignmentApi.ts` — 할당/반납 | ✅ |
| `src/api/biometricApi.ts` — 수집 이력 조회 | ✅ |
| `src/api/monitoringApi.ts` — 할당 현황, 일별 집계 | ✅ |
| `src/api/exportApi.ts` — 엑셀 다운로드 (blob) | ✅ |

### 4-4. 스토어
| 항목 | 상태 |
|---|---|
| `src/stores/authStore.ts` — Zustand persist | ✅ (기존) |
| `src/stores/toastStore.ts` — push/remove | ✅ (기존) |

### 4-5. 공통 UI 컴포넌트
| 항목 | 상태 |
|---|---|
| `Button.tsx` — primary/outline/danger, default/sm, loading | ✅ |
| `Badge.tsx` — ok/warn/danger/blue/purple/gray, dot | ✅ |
| `Modal.tsx` — Portal, 480/360px, ModalHeader/Body/Footer | ✅ |
| `Toast.tsx` — 개별 토스트 아이템 분리 | ✅ |
| `ToastContainer.tsx` — Toast 컴포넌트 활용 리팩터링 | ✅ |
| `Table.tsx` — 제네릭 columns/data, warnRow, emptyState | ✅ |
| `Pagination.tsx` — currentPage/totalPages, 말줄임(...) | ✅ |
| `BatteryBar.tsx` — 20% 이하 danger, 21~30% warn, 이상 ok | ✅ |
| `SummaryCard.tsx` — icon/iconBg/label/value/sub | ✅ |

### 4-6. 레이아웃 컴포넌트
| 항목 | 상태 |
|---|---|
| `Layout.tsx` — Sidebar(210px) + Header(48px) + Content | ✅ |
| `Sidebar.tsx` — 6개 메뉴, 3그룹 (기준데이터/현황모니터링/이력관리) | ✅ |
| `Header.tsx` — 브레드크럼 6개 경로, 로그아웃 | ✅ |

### 4-7. 라우터 및 진입점
| 항목 | 상태 |
|---|---|
| `router/index.tsx` — 8개 페이지 경로 (플레이스홀더) | ✅ |
| `PrivateRoute.tsx` — 미인증 시 /login 리다이렉트 | ✅ |
| `LoginPage.tsx` — 로그인 폼 | ✅ (기존) |

---

## 4. 발견 이슈 및 수정 내역

발견된 이슈 없음

---

## 5. 최종 검증 결과

### 빌드 결과
```
> tsc && vite build

✓ 155 modules transformed.
dist/index.html                      0.59 kB │ gzip:  0.43 kB
dist/assets/index-DLWTE8Hu.css      10.70 kB │ gzip:  3.13 kB
dist/assets/LoginPage-AiOWV6zs.js   39.58 kB │ gzip: 16.12 kB
dist/assets/index-D-SO3epO.js      250.22 kB │ gzip: 81.12 kB
✓ built in 28.99s
```

### 수동 확인 필요 항목
- [ ] `npm run dev` 실행 후 `/` 접속 시 `/login` 리다이렉트 확인
- [ ] 로그인 후 Layout(Sidebar + Header) 렌더링 확인
- [ ] Sidebar 각 메뉴 클릭 시 라우팅 동작 및 활성 상태 확인
- [ ] Toast 컴포넌트 표시 확인 (개발자 콘솔에서 `toastStore.push` 호출)
- [ ] BatteryBar 12%, 47%, 84% 세 가지 색상 확인

---

## 6. 후속 TASK 유의사항
- TASK-05에서 환자/장치 관리 페이지 구현 시 `PlaceholderPage`를 실제 컴포넌트로 교체
- Table, Pagination, Modal, Badge 등 공통 컴포넌트 import하여 사용
- API 함수는 `@/api/patientApi`, `@/api/deviceApi` 등에서 import
- 라우터 경로 `/patients/new`가 `/patients/:id`보다 먼저 매칭되도록 순서 주의 (현재 올바르게 배치됨)

---

## 7. 산출물 목록

### 신규 생성 파일
| 파일 경로 | 역할 |
|---|---|
| `src/types/patient.ts` | 환자 타입 정의 |
| `src/types/device.ts` | 장치 타입 정의 |
| `src/types/assignment.ts` | 할당 타입 정의 |
| `src/types/biometric.ts` | 생체신호/모니터링 타입 정의 |
| `src/api/authApi.ts` | 인증 API |
| `src/api/patientApi.ts` | 환자 CRUD API |
| `src/api/deviceApi.ts` | 장치 CRUD API |
| `src/api/assignmentApi.ts` | 할당/반납 API |
| `src/api/biometricApi.ts` | 수집 이력 API |
| `src/api/monitoringApi.ts` | 모니터링 API |
| `src/api/exportApi.ts` | 엑셀 다운로드 API |
| `src/components/ui/Button.tsx` | 버튼 컴포넌트 |
| `src/components/ui/Badge.tsx` | 배지 컴포넌트 |
| `src/components/ui/Modal.tsx` | 모달 컴포넌트 |
| `src/components/ui/Toast.tsx` | 토스트 아이템 컴포넌트 |
| `src/components/ui/Table.tsx` | 테이블 컴포넌트 |
| `src/components/ui/Pagination.tsx` | 페이지네이션 컴포넌트 |
| `src/components/ui/BatteryBar.tsx` | 배터리 바 컴포넌트 |
| `src/components/ui/SummaryCard.tsx` | 요약 카드 컴포넌트 |
| `src/router/PrivateRoute.tsx` | 인증 라우트 가드 |

### 수정 파일
| 파일 경로 | 변경 내용 |
|---|---|
| `src/styles/global.css` | 수집 항목 분류색 CSS 변수 8종 추가 |
| `src/types/common.ts` | ApiError 인터페이스 추가 |
| `src/components/layout/Layout.tsx` | 인증 체크 제거 (PrivateRoute로 분리) |
| `src/components/layout/Sidebar.tsx` | 메뉴 6개·3그룹으로 확장 |
| `src/components/layout/Header.tsx` | 브레드크럼 6개 경로 대응 |
| `src/components/ui/ToastContainer.tsx` | Toast 컴포넌트 분리 활용 |
| `src/router/index.tsx` | 8개 페이지 경로 + PrivateRoute 적용 |
