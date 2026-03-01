# TASK-04 — Front-end: 초기화 + 공통 컴포넌트

> 선행: TASK-00 | 후속: TASK-05

---

## 작업 목표
React 프로젝트의 공통 컴포넌트(버튼·배지·테이블·모달·토스트)와
레이아웃(사이드바·헤더)을 스타일 가이드 기준으로 구현한다.

---

## 체크리스트

### 4-1. 스타일 설정
- [ ] `src/styles/global.css` — STYLE_GUIDE_WEB.md 섹션 14 CSS 변수 전체 선언
  ```css
  :root {
    --primary: #6B5CE7; --primary-dark: #5647CC; --primary-bg: #EDE9FF;
    --ok: #27AE60; --ok-bg: #E8F8F0;
    --warn: #E67E22; --warn-bg: #FFF3E0;
    --danger: #E74C3C; --danger-bg: #FDECEA;
    --text: #1C2333; --text-sub: #6C7A89;
    --gray-border: #E0E4EA; --gray-light: #F0F2F5;
    --white: #FFFFFF; --sidebar-bg: #181D2E;
    --sidebar-w: 210px; --header-h: 48px;
    /* 수집 항목 분류색 */
    --cat-vital-bg: #FFFDE7; --cat-vital-text: #8A6400;
    --cat-activity-bg: #E3F2FD; --cat-activity-text: #1A5276;
    --cat-sleep-bg: #F3E5F5; --cat-sleep-text: #6C3483;
    --cat-ai-bg: #E8F5E9; --cat-ai-text: #1A6B3C;
  }
  ```

### 4-2. 타입 정의
- [ ] `src/types/common.ts` — ApiResponse, PageResponse, ApiError
- [ ] `src/types/patient.ts` — Patient, PatientStatus, PatientSearchCondition
- [ ] `src/types/device.ts` — Device, DeviceStatus
- [ ] `src/types/assignment.ts` — Assignment, AssignmentStatus
- [ ] `src/types/biometric.ts` — BiometricHistory, DailySummary, MonitoringStatus

### 4-3. API 레이어
- [ ] `src/api/axiosInstance.ts`
  - JWT Bearer 자동 첨부 (요청 인터셉터)
  - 401 응답 시 Refresh Token으로 재발급 후 재시도
  - 재발급 실패 시 로그인 페이지 이동
- [ ] `src/api/authApi.ts` — login, refresh, logout
- [ ] `src/api/patientApi.ts` — getPatients, getPatient, createPatient, updatePatient, deletePatient
- [ ] `src/api/deviceApi.ts` — getDevices, getDevice, createDevice, updateDevice, deleteDevice
- [ ] `src/api/assignmentApi.ts` — getAssignments, assignDevice, returnDevice
- [ ] `src/api/biometricApi.ts` — getBiometricHistory
- [ ] `src/api/monitoringApi.ts` — getAssignmentStatus, getDailySummary
- [ ] `src/api/exportApi.ts` — downloadPatientExcel, downloadAllExcel

### 4-4. 스토어
- [ ] `src/stores/authStore.ts` (Zustand)
  ```ts
  // user, accessToken, isAuthenticated
  // actions: login, logout, setToken
  ```
- [ ] `src/stores/toastStore.ts` (Zustand)
  ```ts
  // toasts: ToastItem[]
  // actions: addToast(ok|warn|info|danger), removeToast
  ```

### 4-5. 공통 UI 컴포넌트
- [ ] `src/components/ui/Button.tsx`
  - variants: primary | outline | danger
  - sizes: default (h-30px) | sm (h-26px, 테이블 내)
  - loading 상태 지원

- [ ] `src/components/ui/Badge.tsx`
  - variants: ok | warn | danger | blue | purple | gray
  - dot 표시 여부 prop

- [ ] `src/components/ui/Modal.tsx`
  - 오버레이 + 박스 (width prop: 480px 기본 / 360px confirm)
  - 진입 애니메이션 (`modalIn` 0.2s ease-out)
  - ModalHeader, ModalBody, ModalFooter 서브컴포넌트

- [ ] `src/components/ui/Toast.tsx` + `ToastContainer.tsx`
  - 우상단 고정 (fixed top-60px right-20px)
  - variants: ok | warn | info | danger (좌측 3px 컬러 보더)
  - 진입 애니메이션 `toastIn` 0.25s

- [ ] `src/components/ui/Table.tsx`
  - thead 배경 `var(--tbl-header)` / hover `#F5F7FF`
  - warn-row prop → 배경 `#FFF8F0`
  - empty state 표시

- [ ] `src/components/ui/Pagination.tsx`
  - 현재 페이지 primary 배경
  - 버튼 크기 26×26px

- [ ] `src/components/ui/BatteryBar.tsx`
  - track 36×10px / fill: 20% 미만 danger, 21~30% warn, 이상 ok
  - 수치 텍스트 우측 표시

- [ ] `src/components/ui/SummaryCard.tsx`
  - 4종 아이콘 배경색 (total/assigned/ok/warn)
  - label, value, sub 슬롯

### 4-6. 레이아웃 컴포넌트
- [ ] `src/components/layout/Layout.tsx`
  - Sidebar (210px 고정) + main (flex:1)
  - main = Header(48px) + Content (overflow-y: auto)

- [ ] `src/components/layout/Sidebar.tsx`
  - 배경 `#181D2E`, 로고 + 메뉴 그룹 구조
  - 활성 메뉴: `#252D48` 배경 + `var(--primary)` 좌측 3px 보더
  - 메뉴 구조:
    ```
    [기준데이터] 환자 정보 관리 / 장치 정보 관리
    [현황모니터링] 장치 할당 현황 / 데이터 현황 집계
    [이력관리] 할당·해제 이력 / 수집 데이터 이력
    ```

- [ ] `src/components/layout/Header.tsx`
  - 브레드크럼 (현재 경로 기반 자동 생성)
  - 알림 아이콘 + 사용자 아바타 + 이름

### 4-7. 라우터 및 진입점
- [ ] `src/router/index.tsx` — 전체 라우팅 구조
  ```
  /login                    → LoginPage
  / (Layout 래핑)
    /patients                → PatientListPage
    /patients/:id            → PatientDetailPage
    /patients/new            → PatientFormPage
    /devices                 → DeviceListPage
    /monitoring/assignments  → AssignmentStatusPage
    /monitoring/summary      → DailySummaryPage
    /history/assignments     → AssignmentHistoryPage
    /history/biometric       → BiometricHistoryPage
  ```
- [ ] `PrivateRoute.tsx` — 미인증 시 /login 리다이렉트
- [ ] `src/pages/LoginPage.tsx` — 관리자 로그인 폼

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-04.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-04

### 생성 파일 목록 (경로 | 역할)

### 컴포넌트 설계 결정사항
(props 인터페이스, 재사용성 고려)

### 라우터 구조 확인

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-04 승인.

docs/STYLE_GUIDE_WEB.md를 먼저 읽고 아래 순서로 작업해줘.
모든 색상·크기는 CSS 변수 사용. HEX 하드코딩 금지.

1. global.css CSS 변수 전체 선언
2. TypeScript 타입 정의 5종
3. API 레이어 (axiosInstance 포함 8종)
4. Zustand 스토어 2종
5. UI 컴포넌트 8종 (Button → Badge → Modal → Toast → Table → Pagination → BatteryBar → SummaryCard)
6. 레이아웃 컴포넌트 3종 (Layout → Sidebar → Header)
7. 라우터 + PrivateRoute + LoginPage

각 컴포넌트는 TypeScript props 인터페이스 명시.
스타일은 Tailwind + CSS 변수 혼용 (Tailwind가 없는 값은 style 속성으로).
```

### Step 3 — 완료 검증
```
TASK-04 완료 검증을 해줘.

1. npm run dev 실행 후 / 접속 시 /login 리다이렉트 확인
2. 로그인 후 Layout (Sidebar + Header) 렌더링 확인
3. 각 메뉴 클릭 시 라우팅 동작 확인
4. Toast 컴포넌트 토스트 표시 확인 (toastStore.addToast 직접 호출)
5. BatteryBar: 12%, 47%, 84% 세 가지 색상 확인

npm run build 오류 없으면 완료.
```

---

## 완료 기준
- [ ] `npm run build` 오류 0건
- [ ] CSS 변수 전체 선언 (--primary #6B5CE7 등)
- [ ] 8개 UI 컴포넌트 렌더링 확인
- [ ] Sidebar 메뉴 활성 상태 정상 표시
- [ ] JWT 인터셉터 401 재발급 로직 포함
- [ ] 라우터 PrivateRoute 동작 확인
