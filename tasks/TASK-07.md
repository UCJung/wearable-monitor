# TASK-07 — Front-end: 이력 관리 + 엑셀 다운로드

> 선행: TASK-06 | 후속: TASK-11
> 관련 기능: FNT-005 (할당/해제 이력), FNT-006 (수집 이력 조회·다운로드)

---

## 작업 목표
장치 할당/해제 이력 조회 화면과 수집 데이터 이력 조회·엑셀 다운로드 화면을 구현한다.

---

## 체크리스트

### 7-1. API 연동 함수
- [ ] `src/api/biometricApi.ts`
- [ ] `src/api/exportApi.ts` (Blob 다운로드 패턴)
- [ ] `src/types/biometric.ts` (BiometricHistoryItem, hasMore 플래그)

### 7-2. FNT-005: 할당/해제 이력 조회
- [ ] `src/pages/history/AssignmentHistoryPage.tsx`
  - 필터 바: 환자명/코드, 시리얼, 기간, 상태(ACTIVE/RETURNED/LOST)
  - 이력 테이블: 상태 배지(ACTIVE=blue, RETURNED=gray, LOST=danger)
  - 해제 사유: 말줄임 + hover tooltip
  - 모니터링 기간 종료일 없으면 "진행 중" 표시

### 7-3. FNT-006: 수집 이력 조회 및 다운로드
- [ ] `src/pages/history/BiometricHistoryPage.tsx`
  - 조회 조건 패널: 환자 Select(필수), 항목 MultiSelect, 기간(최대 90일)
  - 환자 미선택 시 [조회] 버튼 disabled
  - 5,000건 초과 배너: 배경 #FFF8E1, 보더 #FFE082
  - 분류별 행 배경: VITAL_SIGN=#FFFDE7 / ACTIVITY=#E3F2FD / SLEEP=#F3E5F5 / AI_SCORE=#E8F5E9
  - 엑셀 다운로드: Blob → URL.createObjectURL → a 태그 클릭
  - 파일명: `{patientCode}_수집이력_{yyyyMMdd}.xlsx`
- [ ] `src/hooks/useBiometricHistory.ts` (TanStack Query, enabled: !!patientId)

---

## Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-07.md를 읽어줘.
docs/STYLE_GUIDE_WEB.md와 docs/ui_web_monitoring.html도 참고해줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-07
### 생성 파일 목록 (경로 | 역할)
### MultiSelect 컴포넌트 구현 방식
### 엑셀 다운로드 Blob 처리 방식
### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-07 승인.

docs/ui_web_monitoring.html 시안 기준으로 구현해줘.
모든 색상은 CSS 변수 또는 스타일 가이드 HEX 값만 사용해줘.

순서:
1. API 함수 및 타입 정의
2. AssignmentHistoryPage (필터+테이블+배지+tooltip)
3. BiometricHistoryPage (조건 패널+5000건 배너+분류별 배경+다운로드)
4. useBiometricHistory 훅
```

### Step 3 — 완료 검증
```
TASK-07 완료 검증해줘.

1. 환자 미선택 시 [조회] disabled 확인
2. 5000건 초과 배너 로직 확인
3. 분류별 행 배경색 (VITAL_SIGN #FFFDE7) 적용 확인
4. 엑셀 다운로드 파일명 형식 확인
5. npm run build 오류 0건 확인
```

---

## 완료 기준
- [ ] AssignmentHistoryPage 필터·테이블·페이지네이션 동작
- [ ] BiometricHistoryPage 조회 조건 검증 동작
- [ ] 분류별 행 배경색 적용
- [ ] 5,000건 초과 배너 조건부 렌더링
- [ ] 엑셀 다운로드 파일 저장 동작
- [ ] `npm run build` 타입 오류 0건
