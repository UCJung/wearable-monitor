# TASK-03 — Back-end: 수집·조회·모니터링·엑셀 API

> 선행: TASK-02 | 후속: TASK-06, TASK-07, TASK-10
> 관련 기능: FNT-003, FNT-004, FNT-006 (조회·집계·다운로드)

---

## 작업 목표
Android 배치 업로드, 수집 이력 조회, 현황 모니터링 집계,
엑셀 다운로드 API를 구현한다.

---

## 체크리스트

### 3-1. 생체신호 수집 API
- [ ] `BiometricController`
  ```
  POST /api/v1/biometric/batch        Android 배치 업로드 (최대 500건)
  GET  /api/v1/biometric/{patientId}  수집 이력 조회 (기간·항목 필터)
  ```
- [ ] `BiometricService`
  - `batchUpload(List<BiometricUploadItem>)`
    - 중복 체크: `(assignment_id, item_code, measured_at)` UNIQUE
    - 중복 건 스킵(upsert 아닌 skip), 결과 통계 반환
    - 장치 last_sync_at 갱신
  - `getHistory(patientId, itemCodes, start, end, pageable)`
    - 최대 90일 범위 검증
    - 화면용 최대 5,000건 반환 (초과 시 hasMore: true 플래그)
- [ ] DTO
  - `BiometricUploadItem` (itemCode, measuredAt, valueNumeric, valueText, durationSec)
  - `BatchUploadRequest` (List<BiometricUploadItem>, deviceSerialNumber)
  - `BatchUploadResponse` (total, saved, skipped, failed)
  - `BiometricHistoryResponse` (측정 이력 + hasMore 플래그)

### 3-2. 현황 모니터링 API (FNT-003, FNT-004)
- [ ] `MonitoringController`
  ```
  GET /api/v1/monitoring/assignment-status  전체 환자 할당 현황
  GET /api/v1/monitoring/daily-summary      날짜·항목별 집계
  ```
- [ ] `MonitoringService`
  - `getAssignmentStatus()`
    - 전체 환자 + 현재 할당 장치 + 최종 수집 일시 조합
    - isRecentlyCollected: 최종 수집 24시간 이내 여부 계산
    - 요약 카운트: 전체/할당/정상/미수집
  - `getDailySummary(patientIds, itemCodes, start, end)`
    - TimescaleDB `time_bucket('1 day', measured_at)` 활용
    - 환자별·날짜별·항목별 수집 건수 + 평균값 집계
    - 최대 31일 범위 검증
- [ ] DTO
  - `AssignmentStatusResponse` (환자 정보 + 장치 정보 + 수집 상태)
  - `MonitoringSummaryResponse` (카운트 요약 + 환자 목록)
  - `DailySummaryResponse` (날짜별 항목별 집계 결과)

### 3-3. 엑셀 다운로드 API (FNT-006)
- [ ] `ExportController`
  ```
  GET /api/v1/export/patient/{patientId}  환자별 수집 이력 엑셀
  GET /api/v1/export/all                  전체 환자 엑셀 (관리자용)
  ```
- [ ] `ExcelExportService` (Apache POI)
  - **환자별 엑셀** 시트 2종
    - Sheet1: 수집이력 (전체 데이터, 건수 제한 없음)
    - Sheet2: 일별요약 (날짜별 항목별 평균)
  - **전체 엑셀** 시트 4종
    - Sheet1: 환자목록 (patient_code, 이름, 상태, 할당 장치)
    - Sheet2: 수집이력 전체
    - Sheet3: 일별요약
    - Sheet4: 기기현황 (시리얼, 모델, 상태, 배터리, 동기화)
  - 공통 스타일: 헤더 배경 `#1A3A5C`, 흰 텍스트, 자동 열 너비
  - 파일명 형식: `{patientCode}_수집이력_{yyyyMMdd}.xlsx`

### 3-4. 엑셀 수집이력 시트 컬럼 정의

| 컬럼명 | 데이터 | 비고 |
|---|---|---|
| 환자 코드 | patient_code | |
| 환자명 | name | |
| 측정 일시 | measured_at | yyyy-MM-dd HH:mm:ss |
| 항목 코드 | item_code | |
| 항목명 | item_name_ko | |
| 분류 | category 한글 | 생체신호/활동/수면/AI종합 |
| 측정값 | value_numeric or value_text | |
| 단위 | unit | |
| 지속 시간 | duration_sec → H시간 M분 | 수면·운동만 |
| 장치 시리얼 | serial_number | |

### 3-5. 단위 테스트
- [ ] `BiometricServiceTest` — 배치 업로드 중복 스킵, 조회 범위 제한
- [ ] `MonitoringServiceTest` — 24h 수집 여부, 집계 결과
- [ ] `ExcelExportServiceTest` — 시트 구성, 컬럼 수 확인

---

## 🤖 Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-03.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-03

### 생성 파일 목록 (경로 | 역할)

### 배치 업로드 중복 처리 전략
(UNIQUE 제약 + 예외 catch 방식 or 사전 조회 방식 중 선택 이유)

### TimescaleDB time_bucket 쿼리 전략

### 엑셀 생성 성능 고려사항 (대용량 처리)

### 결정 필요 사항
```

### Step 2 — 작업 수행 (승인 후)
```
TASK-03 승인.

아래 순서로 구현해줘.

1. 생체신호 수집 API
   - BatchUpload: UNIQUE 제약 위반 시 개별 catch + skip 방식으로 구현
   - 업로드 완료 시 device.last_sync_at 갱신
2. 현황 모니터링 API
   - assignment-status: 최종 수집 일시 서브쿼리로 효율적으로 조회
   - daily-summary: TimescaleDB time_bucket 활용한 집계 쿼리
3. 엑셀 다운로드 API
   - ExcelExportService: SXSSFWorkbook (스트리밍) 방식으로 메모리 효율적 처리
   - 헤더 스타일: #1A3A5C 배경, 흰 텍스트, 가운데 정렬
   - 응답 헤더: Content-Disposition attachment 설정
4. 단위 테스트 3종

엑셀은 10만 건 이상도 처리 가능하도록 SXSSFWorkbook 사용해줘.
```

### Step 3 — 완료 검증
```
TASK-03 완료 검증을 해줘.

1. POST /api/v1/biometric/batch
   - 50건 업로드 후 saved 카운트 확인
   - 동일 데이터 재업로드 시 skipped 카운트 확인

2. GET /api/v1/monitoring/assignment-status
   - isRecentlyCollected 필드 포함 여부 확인
   - summary (전체/할당/정상/미수집) 카운트 확인

3. GET /api/v1/monitoring/daily-summary?start=2026-02-01&end=2026-02-28
   - 최대 31일 제한 테스트 (32일 요청 시 400)

4. GET /api/v1/export/patient/{id}?start=2026-01-01&end=2026-02-28
   - 엑셀 파일 다운로드 + 시트 2종 확인

./gradlew test 결과 알려줘.
```

---

## 완료 기준
- [ ] 배치 업로드 중복 스킵 정상 동작
- [ ] `last_sync_at` 배치 업로드 시 갱신 확인
- [ ] `isRecentlyCollected` 24h 기준 정확히 계산
- [ ] `daily-summary` 31일 초과 시 400 오류
- [ ] 엑셀 파일 다운로드 정상 + 시트 구성 확인
- [ ] `./gradlew test` 전체 통과
