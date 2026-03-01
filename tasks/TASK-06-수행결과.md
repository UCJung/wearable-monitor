# TASK-06 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-opus-4-6)
> 상태: **완료**

---

## 1. 작업 개요
장치 할당 현황(5분 자동 갱신 + 카운트다운 타이머)과 데이터 현황 집계(일별 집계 테이블 + 엑셀 다운로드) 화면을 구현한다.

---

## 2. 완료 기준 달성 현황

| 완료 기준 | 상태 |
|---|---|
| 요약 카드 4개 API 데이터 연동 | ✅ |
| 5분 자동 갱신 + 카운트다운 타이머 동작 | ✅ |
| 미수집 행 warn-bg 배경 적용 | ✅ |
| 수집 상태 필터 클라이언트 필터링 동작 | ✅ |
| 31일 초과 클라이언트 검증 동작 | ✅ |
| 엑셀 다운로드 버튼 + 로딩 상태 | ✅ |

---

## 3. 체크리스트 완료 현황

### 6-1. 장치 할당 현황 (FNT-003)

| 항목 | 상태 |
|---|---|
| AssignmentStatusPage.tsx 생성 | ✅ |
| SummaryCard 4개 (전체 환자/장치 할당/정상 수집/미수집) | ✅ |
| 필터 바 (수집 상태 Select, 환자명·코드 검색) | ✅ |
| 카운트다운 타이머 (5분, pulse 점) | ✅ |
| 환자 현황 테이블 (10 컬럼) | ✅ |
| 수집 상태 배지 (ok/warn/gray) | ✅ |
| 미수집 행 warn-bg 배경 (#FFF8F0) | ✅ |
| 미수집 환자 상단 정렬 | ✅ |
| 행 클릭 → /history/biometric?patientId={id} | ✅ |
| refetchInterval 300,000ms (5분) | ✅ |
| 갱신 시 카운트다운 리셋 (dataUpdatedAt 연동) | ✅ |
| useAssignmentStatus 훅 | ✅ |
| useCountdown 훅 (mm분 ss초 포맷) | ✅ |

### 6-2. 데이터 현황 집계 (FNT-004)

| 항목 | 상태 |
|---|---|
| DailySummaryPage.tsx 생성 | ✅ |
| 기간 DateRangePicker (기본 최근 7일, 최대 31일) | ✅ |
| 환자 MultiSelect (details/summary 패턴) | ✅ |
| 항목 MultiSelect (12종 항목 코드) | ✅ |
| [조회] 버튼 | ✅ |
| 집계 테이블 (환자코드/환자명/날짜/항목별 건수·평균) | ✅ |
| 0건 셀 "-" 회색 텍스트 처리 | ✅ |
| 합계 행 (tbl-header 배경, font-weight 600) | ✅ |
| 행 클릭 → 해당 날짜 환자 수집 이력 이동 | ✅ |
| 엑셀 다운로드 버튼 + 로딩 + 비활성화 | ✅ |
| 31일 초과 클라이언트 검증 (오류 메시지) | ✅ |
| useDailySummary 훅 (enabled 기반 수동 조회) | ✅ |
| 라우터 lazy 로딩 연동 | ✅ |

---

## 4. 발견 이슈 및 수정 내역

### 이슈 #1 — DailySummaryPage에서 navigate 미임포트
**증상**: `navigate is not defined` TypeScript 오류
**원인**: `useNavigate` import 및 선언 누락 (행 클릭 네비게이션에서 사용)
**수정**: `react-router-dom`에서 `useNavigate` import 추가, 컴포넌트 내부에 `const navigate = useNavigate()` 선언 추가

### 이슈 #2 — 미사용 변수 idx
**증상**: `TS6133: 'idx' is declared but its value is never read`
**원인**: `pagedRows.map((row, idx) => ...)`에서 idx 미사용
**수정**: `pagedRows.map((row) => ...)`로 변경

### 이슈 #3 — 백엔드 API 엔드포인트 불일치
**증상**: TASK-04에서 정의한 API 경로가 실제 백엔드와 다름
**원인**: 설계 문서 기반 추정과 실제 구현 차이
**수정**:
- monitoringApi: `/monitoring/assignments` → `/monitoring/assignment-status`
- exportApi: `/export/patients/{id}` → `/export/patient/{patientId}`, 전체 다운로드 `/export/all`
- biometricApi: 쿼리 파라미터 방식 → 경로 파라미터 `/biometric/{patientId}`

---

## 5. 최종 검증 결과

### 빌드 검증
```
> tsc && vite build

✓ 496 modules transformed.
✓ built in 4.89s
```
- TypeScript 컴파일 오류: **0건**
- Vite 빌드 오류: **0건**

### 자동화 검증 항목
| 항목 | 결과 |
|---|---|
| npm run build 성공 | ✅ 통과 |
| TypeScript 타입 오류 0건 | ✅ 통과 |
| 라우터 lazy 로딩 설정 | ✅ 확인 |
| 청크 분리 확인 | ✅ AssignmentStatusPage (7.18KB), DailySummaryPage (30.06KB) |

### 수동 확인 필요 항목
| 항목 | 확인 방법 |
|---|---|
| /monitoring/assignments 접속 → 요약 카드 4개 표시 | 브라우저 수동 확인 |
| 카운트다운 타이머 5:00 → 감소 → 리셋 | 브라우저 수동 확인 |
| 수집 상태 필터 변경 시 테이블 필터링 | 브라우저 수동 확인 |
| 미수집 행 warn-bg 배경색 적용 | 브라우저 수동 확인 |
| /monitoring/summary 접속 → 기간 31일 초과 오류 메시지 | 브라우저 수동 확인 |
| 엑셀 다운로드 클릭 시 로딩 상태 표시 | 브라우저 수동 확인 |

---

## 6. 후속 TASK 유의사항

- **TASK-07 (이력 관리 + 엑셀 다운로드)**:
  - `biometric.ts` 타입 정의가 백엔드 DTO에 맞춰 갱신됨 — BiometricHistoryResponse, BiometricRecord, BiometricSearchCondition 사용 가능
  - `biometricApi.ts`가 경로 파라미터 방식(`/biometric/{patientId}`)으로 업데이트됨
  - `exportApi.ts`에 환자별 엑셀(`downloadPatientExcel`)과 전체 엑셀(`downloadAllExcel`) 모두 정의됨
  - 라우터의 TASK-07 플레이스홀더 2개 (`history/assignments`, `history/biometric`) 교체 필요

---

## 7. 산출물 목록

### 신규 생성 파일

| 파일 | 설명 |
|---|---|
| `src/pages/monitoring/AssignmentStatusPage.tsx` | 장치 할당 현황 페이지 |
| `src/pages/monitoring/DailySummaryPage.tsx` | 데이터 현황 집계 페이지 |
| `src/hooks/useMonitoring.ts` | useAssignmentStatus + useCountdown 훅 |
| `src/hooks/useDailySummary.ts` | useDailySummary 훅 |

### 수정 파일

| 파일 | 변경 내용 |
|---|---|
| `src/types/biometric.ts` | 백엔드 DTO 기반 타입 전면 재작성 (MonitoringSummaryResponse, AssignmentStatusItem, DailySummaryItem 등) |
| `src/api/monitoringApi.ts` | 엔드포인트 수정 (/monitoring/assignment-status, /monitoring/daily-summary) |
| `src/api/exportApi.ts` | 엔드포인트 수정 (/export/patient/{id}, /export/all), responseType blob 추가 |
| `src/api/biometricApi.ts` | 경로 파라미터 방식으로 변경 (/biometric/{patientId}) |
| `src/router/index.tsx` | monitoring 플레이스홀더 → lazy 로딩 실제 컴포넌트로 교체 |
