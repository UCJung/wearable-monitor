# TASK-11 — 통합 테스트 및 배포

> 선행: TASK-07, TASK-10 | 최종 단계

---

## 작업 목표
전체 시스템 E2E 시나리오 검증, 성능 확인, Docker 기반 배포 설정을 완성한다.

---

## 체크리스트

### 11-1. Back-end 통합 테스트
- [x] `PatientApiIntegrationTest` (Testcontainers + PostgreSQL)
  - 환자 등록 → 조회 → 장치 할당 → 수집 업로드 → 엑셀 다운로드 전 흐름
- [x] `BiometricBatchIntegrationTest`
  - 500건 배치 업로드 성능 측정 (목표: 5초 이내, Testcontainers 환경 반영)
  - 중복 데이터 스킵 정확도 검증
- [x] `ExcelExportIntegrationTest`
  - 1,000건 데이터 엑셀 생성 성능 (목표: 5초 이내)
  - 시트 구성 및 컬럼 수 검증

### 11-2. Front-end 통합 확인
- [ ] 전체 페이지 라우팅 오류 없음 확인 *(수동 확인 필요)*
- [ ] API 연동 (실제 Back-end 연결) 주요 화면 확인 *(수동 확인 필요)*
  - 환자 목록 → 상세 → 장치 할당 모달
  - 장치 할당 현황 5분 자동 갱신
  - 수집 이력 조회 + 엑셀 다운로드
- [ ] 배지·배경색 스타일 가이드 적용 최종 확인 *(수동 확인 필요)*
- [x] `npm run build` → dist 빌드 파일 생성

### 11-3. Android 통합 확인
- [ ] 실기기 또는 에뮬레이터 테스트 *(수동 확인 필요)*
  - 로그인 → 설정 Wizard → 대시보드 전 흐름
  - WorkManager 배치 전송 (백그라운드 로그 확인)
  - Pull-to-Refresh → 데이터 갱신 확인
- [ ] Health Connect 권한 허용 후 데이터 수집 확인 *(수동 확인 필요)*
- [ ] 네트워크 오프라인 → Room 버퍼 저장 → 온라인 복귀 후 전송 확인 *(수동 확인 필요)*

### 11-4. 배포 설정

- [x] **Back-end Dockerfile** (멀티스테이지 빌드)

- [x] **Front-end Dockerfile** (Node 빌드 → Nginx 서빙)

- [x] **nginx.conf** (React SPA 라우팅 + API 프록시)

- [x] **docker-compose.prod.yml** (postgres / redis / backend / frontend 4서비스)

- [x] **환경 변수 파일 템플릿** `.env.example`

- [x] `README.md` 업데이트
  - 개발 환경 실행 방법
  - 운영 환경 배포 방법 (Docker Compose)
  - 테스트 실행 방법
  - 아키텍처 다이어그램
  - 환경 변수 목록

### 11-5. 최종 체크리스트 (UAT 기준)

| 시나리오 | 확인 |
|---|---|
| 관리자 로그인 | [x] 통합 테스트 검증 |
| 환자 등록 (PT-NNNN 채번) | [x] 통합 테스트 검증 |
| 장치 등록 + 할당 | [x] 통합 테스트 검증 |
| Android 로그인 + 설정 Wizard 완료 | [ ] 수동 확인 필요 |
| Health Connect 데이터 자동 수집 (30분) | [ ] 수동 확인 필요 |
| 장치 할당 현황 5분 자동 갱신 | [ ] 수동 확인 필요 |
| 수집 이력 조회 (기간 필터) | [x] 통합 테스트 검증 |
| 엑셀 다운로드 (시트 2종 포함) | [x] 통합 테스트 검증 |
| 장치 해제 → 장치 AVAILABLE 복원 | [ ] 수동 확인 필요 |

---

## Claude Code 프롬프트

### Step 1 — 계획 수립
```
CLAUDE.md와 TASK-11.md를 읽어줘.

읽은 후 계획서만 출력해. 코드는 작성하지 마.

## 계획서: TASK-11
### 통합 테스트 범위 및 우선순위
### 배포 아키텍처 (Docker Compose 구성도)
### 환경 변수 목록 (필수 / 선택)
### 배포 전 체크리스트
### 결정 필요 사항 (운영 서버 스펙, 도메인 등)
```

### Step 2 — 통합 테스트 수행 (승인 후)
```
TASK-11 승인.

아래 순서로 작업해줘.

1. Back-end 통합 테스트 3종 작성 및 실행
   ./gradlew integrationTest 결과 확인

2. Front-end 빌드 + 주요 화면 API 연동 확인
   npm run build 성공 확인

3. 배포 파일 생성
   - backend/Dockerfile
   - frontend/Dockerfile + nginx.conf
   - docker-compose.prod.yml
   - .env.example

4. docker-compose.prod.yml 로컬 실행 테스트
   docker-compose -f docker-compose.prod.yml up -d

5. README.md 최종 업데이트

6. UAT 체크리스트 11개 항목 직접 확인 후 결과 보고
```

### Step 3 — 배포 검증
```
TASK-11 최종 배포 검증해줘.

1. docker-compose.prod.yml up 후 전체 서비스 상태 확인
2. http://localhost/api/v1/patients 헬스체크
3. http://localhost/ 프론트엔드 접속 확인
4. 환자 등록 → 장치 할당 → 수집 데이터 조회 → 엑셀 다운로드 E2E 시나리오 실행
5. 모든 Docker 컨테이너 재시작 후에도 데이터 유지 확인 (볼륨 마운트)

문제가 있으면 수정하고 최종 완료 보고를 해줘.
```

---

## 완료 기준
- [x] 통합 테스트 전체 통과 (9개 테스트 ALL PASSED)
- [x] `npm run build` 성공 (16.73s)
- [x] Dockerfile 2종 작성 완료
- [x] `docker-compose.prod.yml` 작성 완료 (4서비스)
- [ ] UAT 시나리오 11개 항목 전체 통과 — 자동 검증 5/9 완료, 수동 확인 4건 필요
- [x] README.md 실행 방법 문서화 완료
- [x] `.env.example` 환경 변수 템플릿 완성
