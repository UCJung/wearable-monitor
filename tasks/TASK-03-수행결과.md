# TASK-03 수행 결과 보고서

> 작업일: 2026-03-01
> 작업자: Claude Code (claude-opus-4-6)
> 상태: **완료**

---

## 1. 작업 개요
Android 배치 업로드, 수집 이력 조회, 현황 모니터링 집계, 엑셀 다운로드 API를 구현하였다.

---

## 2. 완료 기준 달성 현황

| 항목 | 상태 |
|---|---|
| 배치 업로드 중복 스킵 정상 동작 | ✅ |
| `last_sync_at` 배치 업로드 시 갱신 확인 | ✅ |
| `isRecentlyCollected` 24h 기준 정확히 계산 | ✅ |
| `daily-summary` 31일 초과 시 400 오류 | ✅ |
| 엑셀 파일 다운로드 정상 + 시트 구성 확인 | ✅ |
| `./gradlew test` 전체 통과 | ✅ |

---

## 3. 체크리스트 완료 현황

### 3-1. 생체신호 수집 API

| 항목 | 상태 |
|---|---|
| BiometricController (POST /batch, GET /{patientId}) | ✅ |
| BiometricService.batchUpload — 중복 체크 + 스킵, last_sync_at 갱신 | ✅ |
| BiometricService.getHistory — 90일 범위 검증, 5000건 제한 | ✅ |
| DTO: BiometricUploadItem, BatchUploadRequest, BatchUploadResponse, BiometricHistoryResponse | ✅ |

### 3-2. 현황 모니터링 API

| 항목 | 상태 |
|---|---|
| MonitoringController (GET /assignment-status, GET /daily-summary) | ✅ |
| MonitoringService.getAssignmentStatus — 전체 환자 + 할당 + 수집 상태 조합 | ✅ |
| MonitoringService.getDailySummary — TimescaleDB CAST AS DATE 활용, 31일 범위 검증 | ✅ |
| DTO: AssignmentStatusItem, MonitoringSummaryResponse, DailySummaryResponse | ✅ |

### 3-3. 엑셀 다운로드 API

| 항목 | 상태 |
|---|---|
| ExportController (GET /patient/{id}, GET /all) | ✅ |
| ExcelExportService — SXSSFWorkbook 스트리밍 방식 | ✅ |
| 환자별 엑셀 시트 2종 (수집이력, 일별요약) | ✅ |
| 전체 엑셀 시트 4종 (환자목록, 수집이력, 일별요약, 기기현황) | ✅ |
| 헤더 스타일: #1A3A5C 배경, 흰 텍스트, 가운데 정렬 | ✅ |
| 파일명 형식: {patientCode}_수집이력_{yyyyMMdd}.xlsx | ✅ |

### 3-4. 엑셀 수집이력 시트 컬럼

| 항목 | 상태 |
|---|---|
| 10개 컬럼 정의 (환자코드, 환자명, 측정일시, 항목코드, 항목명, 분류, 측정값, 단위, 지속시간, 장치시리얼) | ✅ |

### 3-5. 단위 테스트

| 항목 | 상태 |
|---|---|
| BiometricServiceTest (7건) — 배치 업로드 중복 스킵, 빈 목록, 500건 초과, last_sync_at, 조회 범위 제한 | ✅ |
| MonitoringServiceTest (5건) — 24h 수집 여부, 미할당 환자, 요약 카운트, 31일 초과 | ✅ |
| ExcelExportServiceTest (3건) — 환자별 시트 2종, 전체 시트 4종, 헤더 스타일 | ✅ |

---

## 4. 발견 이슈 및 수정 내역

### 이슈 #1 — PatientBiometricHistory 생성자 변경에 따른 기존 테스트 컴파일 오류
**증상**: `PatientBiometricHistoryRepositoryTest`에서 `durationSec` 파라미터 누락으로 생성자 매칭 실패
**원인**: `duration_sec` 컬럼 추가로 엔티티 생성자 시그니처가 변경됨
**수정**: 기존 테스트의 생성자 호출에 7번째 인자 `null` 추가

### 이슈 #2 — findDailySummary 시그니처 변경에 따른 기존 테스트 컴파일 오류
**증상**: `PatientBiometricHistoryRepositoryTest.findDailySummary()`에서 `itemCodes` 파라미터 누락
**원인**: `itemCodes` 필터 파라미터를 Custom 인터페이스에 추가함
**수정**: 기존 테스트 호출에 `null` 인자 추가

### 이슈 #3 — PatientRepository.findByStatusNot import 누락
**증상**: `List<Patient>` 반환 타입에 대한 `java.util.List` import 미선언
**원인**: 메서드 추가 시 import문 누락
**수정**: `import java.util.List;` 추가

---

## 5. 최종 검증 결과

### 빌드 결과
```
> Task :compileJava UP-TO-DATE
BUILD SUCCESSFUL
```

### 테스트 결과
```
BiometricServiceTest:    tests=7, failures=0, errors=0
MonitoringServiceTest:   tests=5, failures=0, errors=0
ExcelExportServiceTest:  tests=3, failures=0, errors=0

전체 테스트: BUILD SUCCESSFUL (기존 테스트 포함 전체 통과)
```

### 수동 확인 필요
- [ ] 브라우저에서 엑셀 파일 다운로드 후 시트 구성·데이터 육안 확인
- [ ] 실제 Android 기기에서 배치 업로드 API 호출 테스트
- [ ] 엑셀 헤더 배경색(#1A3A5C) 육안 확인

---

## 6. 후속 TASK 유의사항
- `duration_sec` 컬럼이 `patient_biometric_history` 테이블에 추가됨 — TASK-10 (Android 수집 Worker)에서 수면/운동 항목 업로드 시 `durationSec` 필드 매핑 필요
- `PatientBiometricHistory` 생성자가 7개 파라미터로 변경됨 (기존 6개 → 7개: durationSec 추가)
- `findDailySummary`에 `itemCodes` 파라미터 추가됨 — null 전달 시 전체 항목 조회
- 엑셀 다운로드는 기간 제한 없음 (대용량 시 SXSSFWorkbook으로 메모리 안전)

---

## 7. 산출물 목록

### 신규 생성 파일

| 파일 경로 | 역할 |
|---|---|
| `api/biometric/BiometricController.java` | 생체신호 배치 업로드 + 이력 조회 API |
| `api/biometric/BiometricService.java` | 배치 업로드 (중복 스킵) + 이력 조회 서비스 |
| `api/biometric/dto/BiometricUploadItem.java` | 개별 업로드 항목 DTO |
| `api/biometric/dto/BatchUploadRequest.java` | 배치 업로드 요청 DTO |
| `api/biometric/dto/BatchUploadResponse.java` | 업로드 결과 통계 DTO |
| `api/biometric/dto/BiometricHistoryResponse.java` | 이력 조회 응답 DTO |
| `api/monitoring/MonitoringController.java` | 할당 현황 + 일별 요약 API |
| `api/monitoring/MonitoringService.java` | 모니터링 집계 서비스 |
| `api/monitoring/dto/AssignmentStatusItem.java` | 환자별 할당+수집 상태 DTO |
| `api/monitoring/dto/MonitoringSummaryResponse.java` | 전체 요약 카운트 + 목록 DTO |
| `api/monitoring/dto/DailySummaryResponse.java` | 일별 집계 결과 DTO |
| `api/export/ExportController.java` | 엑셀 다운로드 API |
| `api/export/ExcelExportService.java` | Apache POI 엑셀 생성 (SXSSFWorkbook) |
| `test/.../BiometricServiceTest.java` | 배치 업로드 단위 테스트 (7건) |
| `test/.../MonitoringServiceTest.java` | 모니터링 단위 테스트 (5건) |
| `test/.../ExcelExportServiceTest.java` | 엑셀 생성 단위 테스트 (3건) |

### 수정 파일

| 파일 경로 | 수정 내용 |
|---|---|
| `resources/sql/schema.sql` | `duration_sec INT` 컬럼 추가 |
| `domain/biometric/PatientBiometricHistory.java` | `durationSec` 필드 + 생성자 변경 |
| `domain/biometric/PatientBiometricHistoryRepository.java` | `findLatestMeasuredAt` 메서드 추가 |
| `domain/biometric/PatientBiometricHistoryRepositoryCustom.java` | `findDailySummary`에 `itemCodes` 파라미터 추가 |
| `domain/biometric/PatientBiometricHistoryRepositoryImpl.java` | `findDailySummary` 구현부 `itemCodes` 필터 추가 |
| `domain/patient/PatientRepository.java` | `findByStatusNot` 메서드 추가 |
| `domain/device/Device.java` | `updateLastSyncAt` 메서드 추가 |
| `common/ErrorCode.java` | BATCH_EMPTY, BATCH_SIZE_EXCEEDED, DATE_RANGE_EXCEEDED, ASSIGNMENT_NOT_ACTIVE 추가 |
| `test/.../PatientBiometricHistoryRepositoryTest.java` | 변경된 생성자·메서드 시그니처 반영 |
