# TASK-06 — Front-end: 현황 모니터링 화면

> 선행: TASK-05, TASK-03 | 후속: TASK-07
> 관련 기능: FNT-003 (할당 현황), FNT-004 (데이터 현황 집계)

---

## 작업 목표
장치 할당 현황(5분 자동 갱신)과 데이터 현황 집계 화면을 구현한다.

---

## 체크리스트

### 6-1. 장치 할당 현황 (FNT-003)

- [ ] `AssignmentStatusPage.tsx`

  **상단 요약 카드 4개 (SummaryCard 컴포넌트)**
  - 전체 환자 / 장치 할당 / ✅ 정상 수집 / ⚠️ 미수집

  **필터 바**
  - 수집 상태 Select (전체 / 정상 / 미수집 / 미할당)
  - 환자명·코드 텍스트 검색
  - 다음 갱신 카운트다운 타이머 (5분 카운트다운, pulse 점)

  **환자 현황 테이블**
  - 컬럼: No. / 환자코드 / 환자명 / 장치 시리얼 / 모델명 / 할당일 / 배터리(BatteryBar) / 최종 수집 일시 / 수집 상태(배지) / 액션
  - 수집 상태: ✅ 정상(badge-ok) / ⚠️ 미수집(badge-warn) / — 미할당(badge-gray)
  - 미수집 행 배경: `var(--warn-bg)` `#FFF8F0`
  - 미수집 환자 상단 정렬 (서버 정렬 또는 클라이언트 정렬)
  - 행 클릭 → `/history/biometric?patientId={id}` 이동

  **자동 갱신**
  - TanStack Query `refetchInterval: 300_000` (5분)
  - 카운트다운 타이머: `useInterval` 훅으로 1초마다 감소
  - 갱신 시 카운트다운 리셋

- [ ] `useMonitoringStatus.ts` 커스텀 훅
  - `useAssignmentStatus()` — refetchInterval 5분 설정
  - `useCountdown(seconds)` — 카운트다운 훅 (mm:ss 포맷)

### 6-2. 데이터 현황 집계 (FNT-004)

- [ ] `DailySummaryPage.tsx`

  **조회 조건 패널**
  - 기간 DateRangePicker (기본 최근 7일, 최대 31일)
  - 환자 MultiSelect (전체 또는 특정 환자 선택)
  - 항목 MultiSelect (전체 또는 특정 항목 선택)
  - [조회] 버튼

  **집계 테이블**
  - 컬럼: 환자코드 / 환자명 / 날짜 / 항목별 수집 건수 / 항목별 평균값
  - 수집 0건 셀: "-" 회색 텍스트
  - 하단 합계 행 (배경 `var(--tbl-header)`, 굵은 폰트)
  - 행 클릭 → 해당 날짜 환자 수집 이력 상세로 이동

  **[엑셀 다운로드] 버튼**
  - `GET /api/v1/export/summary` 호출
  - 다운로드 중 버튼 비활성화 + 로딩 표시

  **유효성**
  - 조회 기간 31일 초과 시 오류 메시지 표시 (API 호출 전 클라이언트 검증)

- [ ] `useDailySummary.ts` 커스텀 훅
  - `useDailySummary(condition)` — 수동 조회 (enabled: false, refetch로 트리거)

---

## UI 상세 규칙

| 요소 | 규칙 |
|---|---|
| 수집 상태 배지 | 24h 이내=badge-ok / 24h 초과=badge-warn / 미할당=badge-gray |
| 미수집 행 배경 | `#FFF8F0` (warn-bg) |
| 카운트다운 pulse | `width:6px height:6px rounded-full bg-ok animate-pulse` |
| 집계 0건 셀 | `color: var(--text-sub)`, 텍스트 "-" |
| 합계 행 | `background: var(--tbl-header)`, `font-weight: 600` |
| 자동갱신 간격 | 5분 = 300,000ms |

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-06.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-06

### 생성 파일 목록

### 5분 자동 갱신 + 카운트다운 구현 전략

### DateRangePicker 라이브러리 선택 (shadcn/ui Calendar or date-fns 조합)

### MultiSelect 컴포넌트 구현 방법

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-06 승인.

docs/STYLE_GUIDE_WEB.md와 docs/ui_web_monitoring.html 시안 참고해서 구현해줘.

1. 커스텀 훅
   - useAssignmentStatus (refetchInterval 5분)
   - useCountdown (mm:ss 카운트다운, 5분 시작)
   - useDailySummary (수동 조회)

2. AssignmentStatusPage
   - SummaryCard 4개 (데이터 연동)
   - 필터 바 + 카운트다운 타이머
   - 현황 테이블 (미수집 행 warn-bg 배경)
   - 수집 상태별 배지

3. DailySummaryPage
   - DateRangePicker (31일 제한 검증)
   - MultiSelect (환자, 항목)
   - 집계 테이블 (0건 "-" 처리, 합계 행)
   - 엑셀 다운로드 버튼

자동 갱신 시 카운트다운이 리셋되는 로직을 useEffect로 연동해줘.
```

### Step 3 — 완료 검증
```
TASK-06 완료 검증을 해줘.

1. /monitoring/assignments 접속 → 요약 카드 4개 + 테이블 확인
2. 카운트다운 타이머 작동 확인 (5:00 → 감소 → 0:00 → 갱신 → 5:00 리셋)
3. 수집 상태 필터 변경 시 테이블 필터링 확인
4. /monitoring/summary 접속 → 기간 31일 초과 입력 시 오류 메시지 확인
5. [엑셀 다운로드] 클릭 시 로딩 상태 확인

npm run build 오류 없으면 완료.
```

---

## 완료 기준
- [ ] 요약 카드 4개 API 데이터 연동
- [ ] 5분 자동 갱신 + 카운트다운 타이머 동작
- [ ] 미수집 행 warn-bg 배경 적용
- [ ] 수집 상태 필터 클라이언트 필터링 동작
- [ ] 31일 초과 클라이언트 검증 동작
- [ ] 엑셀 다운로드 버튼 + 로딩 상태
