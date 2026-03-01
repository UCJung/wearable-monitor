# TASK-05 — Front-end: 기준데이터 관리 화면

> 선행: TASK-04, TASK-02 | 후속: TASK-06
> 관련 기능: FNT-001 (환자 관리), FNT-002 (장치 관리)

---

## 작업 목표
환자 목록·등록·수정·상세(장치할당/해제), 장치 목록·등록·수정·상세 화면을
확정 UI 시안(ui_web_monitoring.html) 기준으로 구현한다.

---

## 체크리스트

### 5-1. 환자 관리 (FNT-001)

- [ ] `PatientListPage.tsx`
  - 필터 바: 환자명·코드 검색 / 상태 Select / 장치할당 여부 Select
  - 테이블 컬럼: No. / 환자코드 / 환자명 / 성별 / 진단명 / 상태(배지) / 장치할당(배지) / 등록일 / 액션
  - 장치 할당 중 행: 배경 연한 파랑 (`#EDF2FF`)
  - 상단 [신규 등록] 버튼
  - 페이지당 20건 / Pagination
  - 행 클릭 → 상세 페이지 이동
  - TanStack Query `useQuery(['patients', condition, page])`

- [ ] `PatientDetailPage.tsx`
  - 기본 정보 카드 (patient_code, 이름, 성별, 진단, 등록일 등)
  - 현재 할당 장치 카드
    - 미할당: [장치 할당] 버튼 표시
    - 할당 중: 장치 정보 + 배터리 바 + [장치 해제] 버튼
  - 할당 이력 테이블 (최근 5건)
  - [수정], [삭제] 버튼 (삭제는 확인 모달)
  - 삭제 → 할당 중이면 서버 400 에러 메시지 Toast로 표시

- [ ] `PatientFormPage.tsx` (`/patients/new`, `/patients/:id/edit`)
  - react-hook-form + zod 유효성
  - 필드: 환자명(필수), 생년월일(필수), 성별(Radio M/F), 진단코드, 진단명, 등록일(필수), 종료일, 특이사항
  - 저장 성공 → 상세 페이지 이동 + 성공 Toast

- [ ] `AssignDeviceModal.tsx`
  - AVAILABLE 장치 목록 Select (동적 조회)
  - 모니터링 시작일, 종료일, 할당 목적 입력
  - 확인 → `useMutation` 처리 + 성공 Toast + 모달 닫기

- [ ] `ReturnDeviceModal.tsx` (확인 다이얼로그)
  - 해제할 장치 정보 표시
  - 해제 사유 입력 (선택)
  - [해제 확인] danger 버튼

### 5-2. 장치 관리 (FNT-002)

- [ ] `DeviceListPage.tsx`
  - 필터: 시리얼 검색 / 상태 Select
  - 테이블: 시리얼 번호 / 모델명 / 상태(배지) / 할당 환자 / 배터리(BatteryBar) / 최종 동기화 / 액션
  - 배터리 20% 미만 → 빨강 강조
  - 동기화 24h 초과 → 주황 강조
  - [신규 등록] 버튼

- [ ] `DeviceDetailPage.tsx`
  - 기본 정보 카드
  - 현재 할당 환자 카드 (없으면 "미할당" 표시)
  - [수정], [삭제] 버튼

- [ ] `DeviceFormModal.tsx` (등록·수정 모달, 인라인)
  - 시리얼 번호(필수, 신규 시), 모델명, 상태, 메모

### 5-3. 커스텀 훅
- [ ] `src/hooks/usePatients.ts`
  - `usePatientList(condition)` — TanStack Query
  - `usePatientDetail(id)`
  - `useCreatePatient()` — useMutation
  - `useUpdatePatient()` — useMutation
  - `useDeletePatient()` — useMutation + 캐시 무효화

- [ ] `src/hooks/useDevices.ts`
  - `useDeviceList(condition)`
  - `useAssignDevice()` — useMutation
  - `useReturnDevice()` — useMutation

---

## UI 상세 규칙 (ui_web_monitoring.html 시안 기준)

| 요소 | 규칙 |
|---|---|
| 상태 배지 | ACTIVE=badge-ok / INACTIVE=badge-gray / COMPLETED=badge-blue |
| 장치 할당 배지 | 할당=badge-blue / 미할당=badge-gray |
| 장치 상태 배지 | AVAILABLE=ok / ASSIGNED=blue / MAINTENANCE=warn / RETIRED=gray |
| 할당 중 환자 행 | background: #EDF2FF |
| 배터리 20% 미만 | 빨강 텍스트 + BatteryBar danger |
| 동기화 24h 초과 | 텍스트 var(--warn) |
| 삭제 확인 모달 | confirm-modal (width 360px) |

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-05.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-05

### 생성 파일 목록 (경로 | 역할)

### TanStack Query 캐시 전략
(쿼리 키, staleTime, invalidate 시점)

### 폼 유효성 규칙 요약

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-05 승인.

docs/STYLE_GUIDE_WEB.md와 docs/ui_web_monitoring.html 시안을 참고해서
아래 순서로 구현해줘. 색상은 CSS 변수만 사용.

1. 커스텀 훅: usePatients.ts, useDevices.ts
2. 환자 관리
   - PatientListPage (필터 + 테이블 + 페이지네이션)
   - PatientFormPage (react-hook-form + zod)
   - PatientDetailPage (카드 + 이력 테이블)
   - AssignDeviceModal / ReturnDeviceModal
3. 장치 관리
   - DeviceListPage (배터리 색상 강조 포함)
   - DeviceDetailPage
   - DeviceFormModal

각 페이지 완성 시 체크리스트 체크.
API 연동은 실제 백엔드가 없을 수 있으므로 MSW mock 또는
axiosInstance baseURL을 환경변수로 처리해줘.
```

### Step 3 — 완료 검증
```
TASK-05 완료 검증을 해줘.

1. /patients 접속 → 목록 페이지 필터 + 테이블 렌더링 확인
2. [신규 등록] → 폼 페이지 이동, 유효성 오류 메시지 확인
3. 환자 행 클릭 → 상세 페이지, 장치 할당 카드 확인
4. [장치 할당] 모달 열림 + 닫힘 확인
5. /devices 접속 → BatteryBar 3단계 색상 확인

npm run build 오류 없으면 완료.
```

---

## 완료 기준
- [ ] 환자 목록·상세·등록·수정 화면 렌더링 확인
- [ ] 장치 목록·상세·등록 화면 렌더링 확인
- [ ] 장치 할당/해제 모달 정상 동작
- [ ] react-hook-form + zod 유효성 오류 표시
- [ ] BatteryBar 3단계 색상 확인
- [ ] TanStack Query 캐시 무효화 (등록 후 목록 자동 갱신)
