# 웨어러블 환자 모니터링 시스템 — Android 앱 스타일 가이드

> **대상:** Android (Kotlin + XML Layout / Jetpack Compose 선택)  
> **기준 해상도:** 360×760dp (Galaxy 계열 기본)  
> **최종 확정 레이아웃 기준** — 2026년 2월

---

## 목차

1. [디자인 원칙](#1-디자인-원칙)
2. [색상 시스템](#2-색상-시스템)
3. [타이포그래피](#3-타이포그래피)
4. [레이아웃 구조](#4-레이아웃-구조)
5. [컴포넌트 — 버튼](#5-컴포넌트--버튼)
6. [컴포넌트 — 입력 필드](#6-컴포넌트--입력-필드)
7. [컴포넌트 — 상태 배지](#7-컴포넌트--상태-배지)
8. [컴포넌트 — 데이터 카드](#8-컴포넌트--데이터-카드)
9. [컴포넌트 — 설정 Wizard](#9-컴포넌트--설정-wizard)
10. [컴포넌트 — 바텀 내비게이션](#10-컴포넌트--바텀-내비게이션)
11. [화면별 레이아웃 명세](#11-화면별-레이아웃-명세)
12. [수집 항목 카드 상세 규칙](#12-수집-항목-카드-상세-규칙)
13. [상태별 시각 규칙](#13-상태별-시각-규칙)
14. [colors.xml 전체 목록](#14-colorsxml-전체-목록)
15. [dimens.xml 전체 목록](#15-dimensxml-전체-목록)

---

## 1. 디자인 원칙

| 원칙 | 설명 |
|------|------|
| **카드 중심** | 모든 데이터는 카드 단위로 그룹화. 카드 간 8dp 간격 |
| **상태 즉시 인식** | 워치 연결 상태·수집 이상·배터리 부족을 색+아이콘으로 진입 즉시 파악 가능 |
| **단순한 인터랙션** | 환자(노인 포함) 대상이므로 탭 영역 최소 44dp, 텍스트 최소 12sp |
| **오프라인 우선** | 네트워크 없이도 최근 수집 데이터 표시 (Room DB 버퍼) |
| **웹과 색상 통일** | Primary, 상태 색상(OK/Warn/Danger)은 웹 스타일 가이드와 동일 |

---

## 2. 색상 시스템

### 2.1 `colors.xml` 정의 색상 — 웹과 동일 계열

```xml
<!-- res/values/colors.xml -->
<resources>
  <!-- Primary -->
  <color name="primary">#6B5CE7</color>
  <color name="primary_dark">#5647CC</color>
  <color name="primary_bg">#EDE9FF</color>
  <color name="accent">#F5A623</color>

  <!-- Semantic -->
  <color name="ok">#27AE60</color>
  <color name="ok_bg">#E8F8F0</color>
  <color name="warn">#E67E22</color>
  <color name="warn_bg">#FFF3E0</color>
  <color name="danger">#E74C3C</color>
  <color name="danger_bg">#FDECEA</color>

  <!-- Neutral -->
  <color name="text_primary">#1C2333</color>
  <color name="text_secondary">#6C7A89</color>
  <color name="border">#E0E4EA</color>
  <color name="bg_page">#F0F2F5</color>
  <color name="bg_card">#FFFFFF</color>
  <color name="bg_input">#F4F6FA</color>

  <!-- 수집 항목 분류 배경 -->
  <color name="cat_vital_bg">#FFFDE7</color>
  <color name="cat_vital_text">#8A6400</color>
  <color name="cat_activity_bg">#E3F2FD</color>
  <color name="cat_activity_text">#1A5276</color>
  <color name="cat_sleep_bg">#F3E5F5</color>
  <color name="cat_sleep_text">#6C3483</color>
  <color name="cat_ai_bg">#E8F5E9</color>
  <color name="cat_ai_text">#1A6B3C</color>

  <!-- 앱 배경 (다크 헤더) -->
  <color name="header_dark">#181D2E</color>
  <color name="white">#FFFFFF</color>
</resources>
```

### 2.2 그라디언트 (헤더)

```xml
<!-- res/drawable/bg_header_gradient.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
  <gradient
    android:startColor="#6B5CE7"
    android:endColor="#8A7EE8"
    android:angle="135" />
</shape>

<!-- 경고 상태 헤더 -->
<!-- bg_header_gradient_warn.xml -->
<gradient
  android:startColor="#B95E00"
  android:endColor="#E67E22"
  android:angle="135" />
```

---

## 3. 타이포그래피

### 3.1 폰트

```xml
<!-- res/font/ 폴더에 추가 또는 Google Fonts API -->
<!-- 기본 시스템 폰트 사용 (Noto Sans KR 자동 적용) -->
```

```gradle
// build.gradle (app)
// Noto Sans KR 사용 시 downloadable fonts 또는 직접 번들
```

### 3.2 텍스트 크기 체계

| 역할 | sp | 굵기 | 색상 |
|------|-----|------|------|
| 앱 헤더 제목 | `17sp` | `700` | white |
| 섹션 타이틀 | `15sp` | `700` | `text_primary` |
| 카드 값 (큰 숫자) | `22sp` | `700` | `text_primary` |
| 카드 라벨 | `11.5sp` | `500` | `text_secondary` |
| 기본 본문 | `13sp` | `400` | `text_primary` |
| 보조 설명 | `11sp` | `400` | `text_secondary` |
| 배지 텍스트 | `11sp` | `600` | 상태별 |
| 카드 섹션 타이틀 | `11.5sp` | `700` | `text_secondary` (대문자) |
| Wizard 타이틀 | `15sp` | `700` | `text_primary` |
| 버튼 텍스트 | `13.5sp` | `600` | white / text_primary |

```xml
<!-- res/values/styles.xml -->
<style name="TextAppearance.App.Title">
  <item name="android:textSize">17sp</item>
  <item name="android:fontWeight">700</item>
  <item name="android:textColor">@color/white</item>
</style>

<style name="TextAppearance.App.CardValue">
  <item name="android:textSize">22sp</item>
  <item name="android:fontWeight">700</item>
  <item name="android:textColor">@color/text_primary</item>
</style>

<style name="TextAppearance.App.Body">
  <item name="android:textSize">13sp</item>
  <item name="android:textColor">@color/text_primary</item>
</style>

<style name="TextAppearance.App.Caption">
  <item name="android:textSize">11sp</item>
  <item name="android:textColor">@color/text_secondary</item>
</style>
```

---

## 4. 레이아웃 구조

### 4.1 전체 앱 구조

```
┌──────────────────────────┐
│  Status Bar (28dp)        │  ← 시스템 상태 바
├──────────────────────────┤
│  App Header               │  ← 그라디언트 / 다크 배경
│  (패딩: 16dp, pb: 20-24dp)│    제목 + 서브타이틀 + 상태 배지
├──────────────────────────┤
│  Scroll Content           │  ← 배경 #F0F2F5
│  (padding: 12dp 14dp)     │    섹션 타이틀 + 카드 그리드
│                           │
│  [섹션 타이틀 11.5sp]     │  margin: 10dp 0 8dp
│  [카드 그리드 2열]        │  gap: 8dp
│  [풀 와이드 카드]         │  grid-column: 1/-1
│                           │
├──────────────────────────┤
│  Bottom Navigation (60dp) │  ← 흰 배경 + 상단 테두리
└──────────────────────────┘
```

### 4.2 화면 패딩 / 마진

```xml
<!-- 전체 콘텐츠 스크롤 영역 -->
android:paddingHorizontal="14dp"
android:paddingVertical="12dp"

<!-- 카드 그리드 gap -->
app:spanCount="2"
<!-- RecyclerView GridLayoutManager spacing: 8dp -->

<!-- 섹션 타이틀 마진 -->
android:marginTop="10dp"
android:marginBottom="8dp"
```

---

## 5. 컴포넌트 — 버튼

### 5.1 Primary 버튼

```xml
<!-- 높이 44dp, 전체 너비 또는 지정 너비 -->
<com.google.android.material.button.MaterialButton
  android:layout_width="match_parent"
  android:layout_height="44dp"
  android:text="로그인"
  android:textSize="15sp"
  android:fontWeight="600"
  app:cornerRadius="12dp"
  app:backgroundTint="@color/primary"
  android:textColor="@color/white" />
```

### 5.2 Outline 버튼

```xml
<com.google.android.material.button.MaterialButton
  style="@style/Widget.MaterialComponents.Button.OutlinedButton"
  android:layout_height="44dp"
  app:cornerRadius="10dp"
  app:strokeColor="@color/border"
  app:strokeWidth="1.5dp"
  android:textColor="@color/text_primary" />
```

### 5.3 Wizard 버튼 (이전/다음)

```xml
<!-- 이전 버튼: flex:1 -->
<MaterialButton
  android:layout_width="0dp"
  android:layout_weight="1"
  android:layout_height="44dp"
  style="OutlinedButton"
  android:text="이전"
  app:cornerRadius="10dp" />

<!-- 다음 버튼: flex:2 -->
<MaterialButton
  android:layout_width="0dp"
  android:layout_weight="2"
  android:layout_height="44dp"
  android:text="다음 →"
  app:cornerRadius="10dp"
  app:backgroundTint="@color/primary" />
```

### 5.4 버튼 크기 규칙

| 종류 | 높이 | 모서리 | 사용처 |
|------|------|--------|--------|
| Primary 대형 | `48dp` | `12dp` | 로그인 버튼 |
| Primary 표준 | `44dp` | `10dp` | Wizard, 대시보드 액션 |
| Outline 표준 | `44dp` | `10dp` | 이전, 취소 |
| 경고 배너 버튼 | `36dp` | `8dp` | 배너 내 액션 |

---

## 6. 컴포넌트 — 입력 필드

### 6.1 로그인 입력 필드

```xml
<com.google.android.material.textfield.TextInputLayout
  style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
  android:layout_height="wrap_content"
  app:boxCornerRadiusTopStart="10dp"
  app:boxCornerRadiusTopEnd="10dp"
  app:boxCornerRadiusBottomStart="10dp"
  app:boxCornerRadiusBottomEnd="10dp"
  app:boxStrokeColor="@color/border"
  app:boxStrokeColorFocused="@color/primary">

  <com.google.android.material.textfield.TextInputEditText
    android:layout_width="match_parent"
    android:layout_height="44dp"
    android:textSize="14sp"
    android:textColor="@color/text_primary"
    android:hint="예: PT-0001" />

</com.google.android.material.textfield.TextInputLayout>
```

### 6.2 오류 상태

```xml
<!-- 프로그래밍 방식 -->
textInputLayout.error = "비밀번호가 올바르지 않습니다."
textInputLayout.isErrorEnabled = true

<!-- 오류 색상 -->
app:errorTextColor="@color/danger"
app:boxStrokeErrorColor="@color/danger"
```

---

## 7. 컴포넌트 — 상태 배지

```xml
<!-- res/drawable/bg_badge_ok.xml -->
<shape android:shape="rectangle">
  <solid android:color="@color/ok_bg" />
  <corners android:radius="20dp" />
</shape>

<!-- 배지 TextView -->
<TextView
  android:layout_height="22dp"
  android:paddingHorizontal="8dp"
  android:textSize="11sp"
  android:fontWeight="600"
  android:background="@drawable/bg_badge_ok"
  android:textColor="@color/ok"
  android:text="✅ 정상" />
```

### 배지 종류

| 배지 | background drawable | textColor |
|------|--------------------|-----------| 
| 정상 | `bg_badge_ok` (`#E8F8F0`) | `@color/ok` |
| 미수집 | `bg_badge_warn` (`#FFF3E0`) | `@color/warn` |
| 위험/오류 | `bg_badge_danger` (`#FDECEA`) | `@color/danger` |
| 미할당 | `bg_badge_gray` (`#F0F2F5`) | `@color/text_secondary` |
| 연결됨 | `bg_badge_ok` | `@color/ok` |
| 연결 끊김 | `bg_badge_danger` | `@color/danger` |

---

## 8. 컴포넌트 — 데이터 카드

### 8.1 기본 카드 (2열 그리드)

```xml
<com.google.android.material.card.MaterialCardView
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  app:cardCornerRadius="14dp"
  app:cardElevation="1dp"
  app:cardBackgroundColor="@color/bg_card"
  android:layout_margin="0dp">

  <LinearLayout
    android:orientation="vertical"
    android:padding="12dp"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <!-- 상단: 아이콘 + 부가 배지 -->
    <LinearLayout android:orientation="horizontal"
      android:layout_marginBottom="4dp">
      <TextView android:text="❤️" android:textSize="18sp" />
      <!-- 선택적 배지 -->
    </LinearLayout>

    <!-- 라벨 -->
    <TextView
      android:text="심박수"
      android:textSize="11.5sp"
      android:textColor="@color/text_secondary" />

    <!-- 값 + 단위 -->
    <LinearLayout android:orientation="horizontal"
      android:gravity="center_vertical">
      <TextView
        android:text="74"
        android:textSize="22sp"
        android:fontWeight="700"
        android:textColor="@color/text_primary" />
      <TextView
        android:text="BPM"
        android:textSize="11sp"
        android:textColor="@color/text_secondary"
        android:layout_marginStart="3dp" />
    </LinearLayout>

    <!-- 서브 텍스트 -->
    <TextView
      android:text="최소 58 · 최대 112"
      android:textSize="10.5sp"
      android:textColor="@color/text_secondary" />
  </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### 8.2 풀 와이드 카드 (걸음 수, 수면)

```xml
<!-- 그리드에서 span 2 처리 -->
<!-- GridLayoutManager에서 SpanSizeLookup 구현 필요 -->
<!-- 또는 FlexboxLayout 사용 -->
```

### 8.3 카드 배경색 분류 (수집 항목별)

```xml
<!-- 생체신호 카드 배경 -->
app:cardBackgroundColor="@color/cat_vital_bg"  <!-- #FFFDE7 -->

<!-- 활동 카드 배경 -->
app:cardBackgroundColor="@color/cat_activity_bg"  <!-- #E3F2FD -->

<!-- 수면 카드 배경 -->
app:cardBackgroundColor="@color/cat_sleep_bg"  <!-- #F3E5F5 -->

<!-- AI 종합 카드 배경 -->
app:cardBackgroundColor="@color/cat_ai_bg"  <!-- #E8F5E9 -->
```

---

## 9. 컴포넌트 — 설정 Wizard

### 9.1 Step Indicator (상단 프로그레스)

```xml
<!-- 헤더 내 스텝 인디케이터 -->
<!-- 커스텀 뷰 또는 StepView 라이브러리 사용 -->
```

```kotlin
// StepIndicatorView 상태값
enum class StepState { DONE, CURRENT, TODO }

// 각 스텝 노드 크기: 28dp × 28dp
// 완료: 흰 배경 + primary 텍스트
// 현재: 흰 배경 + primary 텍스트 + 4dp 흰 테두리 글로우
// 미진행: 흰 25% 투명 배경 + 70% 흰 텍스트
// 연결선: 2dp, 완료=흰 80%, 미진행=흰 30%
```

### 9.2 Step 진행 상태별 헤더 배경

| 모든 단계 | 배경 |
|-----------|------|
| 기본 (1~5단계) | `#6B5CE7` → `#8A7EE8` 그라디언트 |

### 9.3 권한 항목 리스트 (STEP 1)

```xml
<LinearLayout
  android:background="@drawable/bg_perm_item"  <!-- #F0F2F5 배경, 10dp 모서리 -->
  android:padding="11dp"
  android:orientation="horizontal"
  android:gravity="center_vertical">

  <TextView android:text="❤️" android:textSize="20sp" android:layout_marginEnd="10dp"/>

  <LinearLayout android:orientation="vertical" android:layout_weight="1">
    <TextView android:text="심박수" android:textSize="13sp" android:fontWeight="600"/>
    <TextView android:text="READ_HEART_RATE" android:textSize="11sp" android:textColor="@color/text_secondary"/>
  </LinearLayout>

  <!-- 상태 -->
  <TextView
    android:text="허용 필요"
    android:textSize="12sp"
    android:fontWeight="600"
    android:textColor="@color/primary" />
  <!-- 완료 시: android:text="✓" android:textColor="@color/ok" -->
</LinearLayout>
```

### 9.4 Wizard Footer 버튼 영역

```xml
<LinearLayout
  android:layout_height="68dp"
  android:orientation="horizontal"
  android:padding="12dp 12dp"
  android:background="@color/bg_card"
  android:divider="@drawable/divider_top"
  android:showDividers="beginning"
  android:gravity="center_vertical"
  android:gap="8dp">

  <!-- 이전 버튼 (weight 1) -->
  <!-- 다음/완료 버튼 (weight 2) -->
  <!-- 필수 단계 미완료 시 다음 버튼 비활성화 -->
</LinearLayout>
```

---

## 10. 컴포넌트 — 바텀 내비게이션

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
  android:layout_height="60dp"
  android:background="@color/bg_card"
  app:itemIconTint="@color/nav_item_color"       <!-- 비활성: #6C7A89, 활성: #6B5CE7 -->
  app:itemTextColor="@color/nav_item_color"
  app:selectedItemId="@id/nav_dashboard"
  app:elevation="8dp">
  <!-- 상단 1dp 구분선 -->
</com.google.android.material.bottomnavigation.BottomNavigationView>
```

```xml
<!-- res/menu/bottom_nav_menu.xml -->
<menu>
  <item android:id="@+id/nav_dashboard"
    android:icon="@drawable/ic_chart"
    android:title="현황" />
  <item android:id="@+id/nav_history"
    android:icon="@drawable/ic_calendar"
    android:title="이력" />
  <item android:id="@+id/nav_settings"
    android:icon="@drawable/ic_settings"
    android:title="설정" />
</menu>
```

---

## 11. 화면별 레이아웃 명세

### 11.1 로그인 화면

```
배경: Linear Gradient (#F5F3FF → #FFFFFF, 160도)
중앙 정렬 수직 배치:
  ⌚ 로고 아이콘 (48sp)           margin-bottom: 16dp
  앱 이름 (20sp, 700)             margin-bottom: 4dp
  서브 타이틀 (12.5sp, gray)      margin-bottom: 32dp
  [환자 ID 입력]                  margin-bottom: 12dp
  [비밀번호 입력 + 눈 아이콘]      margin-bottom: 8dp
  [로그인 버튼 (48dp, primary)]
  힌트 텍스트 (11.5sp, gray)      margin-top: 16dp
  오류 배너 (danger 배경)         오류 발생 시 표시
```

### 11.2 현황 대시보드

```
Header (그라디언트):
  - 제목: "안녕하세요, {이름}님 👋" (17sp, bold)
  - 서브: 날짜 표시 (12sp)
  - 우측: 워치 연결 상태 배지 (pill)
  - 하단: 마지막 동기화 일시 (11sp, 우정렬)

DatePicker 바 (선택 날짜 표시, 기본: 오늘)

ScrollView 콘텐츠 (bg: #F0F2F5, padding: 12dp 14dp):
  섹션 [❤️ 생체신호]
    2열 그리드 카드: 심박수 / SpO2 / 스트레스 / 피부 온도

  섹션 [🏃 활동]
    풀 와이드: 걸음 수 + 진행바
    2열 그리드: 소모 칼로리 / 운동 시간

  섹션 [😴 수면]
    풀 와이드: 수면 시간 + 단계 가로 바

  섹션 [⚡ AI 종합]
    풀 와이드: 에너지 점수 원형 게이지 + 수치

Bottom Navigation (60dp)
```

### 11.3 경고 상태 (미수집)

```
Header: warn 그라디언트 (#B95E00 → #E67E22)
  - 우측: "연결 끊김" 배지 (danger 계열)
  - 동기화: "마지막 동기화: {N}시간 전"

경고 카드 (warn_bg + warn 테두리):
  ⚠️ 데이터 미수집 경고
  설명 텍스트
  [워치 연결 설정하기] 버튼

배터리 경고 카드 (danger_bg + danger 테두리):
  🪫 배터리 부족
  남은 용량 및 충전 안내

데이터 카드: opacity 50% (수집 없음 표시)
```

---

## 12. 수집 항목 카드 상세 규칙

### 12.1 걸음 수 카드 (풀 와이드)

```
[아이콘] 걸음 수          [목표: 10,000보]
4,721                  걸음
━━━━━━━━━━━━━━━━━━━━━━━
████████████████░░░░░  47.2%

진행 바: 6dp 높이, border-radius 4dp
색상: primary → primary_dark 그라디언트
배경: #F0F2F5
```

### 12.2 수면 단계 카드 (풀 와이드)

```
[😴] 수면 시간          [수면 점수 78]
6.5                    시간

[깊은잠][  렘  ][얕][깊은잠][  렘  ]   ← 가로 막대, 8dp 높이
깊은잠 ■ / REM ■ / 얕은잠/깸 □         ← 범례 (10sp)

색상:
  깊은잠: #5647CC (primary_dark)
  REM: #8A7EE8
  얕은잠/깸: #E0E0E0
```

### 12.3 에너지 점수 카드 (풀 와이드)

```
[원형 게이지 64dp × 64dp]    에너지 점수
       78                   78 / 100
                            🟡 보통 수준 · 충분한 수면 권장

원형 게이지:
  배경 원: stroke #E8E0FF, 6dp
  진행 원: stroke primary, 6dp, strokeLineCap=round
  내부 숫자: 15sp, bold, primary 색상

점수별 색상:
  80 이상: ok (#27AE60)
  50~79: accent (#F5A623)
  50 미만: danger (#E74C3C)
```

---

## 13. 상태별 시각 규칙

### 13.1 워치 연결 상태

| 상태 | 배지 배경 | 점 색상 | 텍스트 |
|------|-----------|---------|--------|
| 연결됨 | 흰 20% 투명 | `#4ADE80` | "연결됨" |
| 연결 끊김 | 흰 20% 투명 | `#FF6B6B` | "연결 끊김" |

### 13.2 마지막 동기화 경과 시간

| 경과 시간 | 텍스트 색상 | 표시 |
|-----------|-------------|------|
| 1시간 미만 | white 85% | "09:32" |
| 1~3시간 | `#FFD700` | "1시간 32분 전" |
| 3시간 이상 | `#FF6B6B` | "3시간 15분 전 ⚠️" |

### 13.3 데이터 없는 카드

```xml
<!-- 수집 데이터 없을 때 -->
<TextView android:text="—" android:textSize="22sp" android:alpha="0.5" />
<TextView android:text="수집 없음" android:textSize="10.5sp" android:alpha="0.5" />
```

### 13.4 권한 항목 상태

| 상태 | 우측 텍스트 | 색상 |
|------|------------|------|
| 허용 필요 | "허용 필요" | `primary` |
| 허용됨 | "✓" | `ok` |
| 거부됨 | "허용 안됨" | `danger` |

---

## 14. `colors.xml` 전체 목록

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <!-- Primary -->
  <color name="primary">#6B5CE7</color>
  <color name="primary_dark">#5647CC</color>
  <color name="primary_bg">#EDE9FF</color>
  <color name="accent">#F5A623</color>

  <!-- Semantic -->
  <color name="ok">#27AE60</color>
  <color name="ok_bg">#E8F8F0</color>
  <color name="warn">#E67E22</color>
  <color name="warn_bg">#FFF3E0</color>
  <color name="danger">#E74C3C</color>
  <color name="danger_bg">#FDECEA</color>

  <!-- Neutral -->
  <color name="text_primary">#1C2333</color>
  <color name="text_secondary">#6C7A89</color>
  <color name="border">#E0E4EA</color>
  <color name="bg_page">#F0F2F5</color>
  <color name="bg_card">#FFFFFF</color>
  <color name="bg_input">#F4F6FA</color>
  <color name="white">#FFFFFF</color>
  <color name="divider">#F0F2F5</color>

  <!-- Category -->
  <color name="cat_vital_bg">#FFFDE7</color>
  <color name="cat_vital_text">#8A6400</color>
  <color name="cat_activity_bg">#E3F2FD</color>
  <color name="cat_activity_text">#1A5276</color>
  <color name="cat_sleep_bg">#F3E5F5</color>
  <color name="cat_sleep_text">#6C3483</color>
  <color name="cat_ai_bg">#E8F5E9</color>
  <color name="cat_ai_text">#1A6B3C</color>

  <!-- Special -->
  <color name="header_dark">#181D2E</color>
  <color name="sleep_deep">#5647CC</color>
  <color name="sleep_rem">#8A7EE8</color>
  <color name="sleep_light">#E0E0E0</color>
  <color name="step_gauge_end">#8A7EE8</color>
</resources>
```

---

## 15. `dimens.xml` 전체 목록

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <!-- 레이아웃 -->
  <dimen name="content_padding_horizontal">14dp</dimen>
  <dimen name="content_padding_vertical">12dp</dimen>
  <dimen name="card_grid_gap">8dp</dimen>
  <dimen name="section_title_margin_top">10dp</dimen>
  <dimen name="section_title_margin_bottom">8dp</dimen>

  <!-- 카드 -->
  <dimen name="card_corner_radius">14dp</dimen>
  <dimen name="card_padding">12dp</dimen>
  <dimen name="card_elevation">1dp</dimen>

  <!-- 버튼 -->
  <dimen name="btn_height_large">48dp</dimen>
  <dimen name="btn_height_standard">44dp</dimen>
  <dimen name="btn_height_small">36dp</dimen>
  <dimen name="btn_corner_large">12dp</dimen>
  <dimen name="btn_corner_standard">10dp</dimen>
  <dimen name="btn_corner_small">8dp</dimen>

  <!-- 입력 필드 -->
  <dimen name="input_height">44dp</dimen>
  <dimen name="input_corner_radius">10dp</dimen>
  <dimen name="input_stroke_width">1.5dp</dimen>

  <!-- 배지 -->
  <dimen name="badge_height">22dp</dimen>
  <dimen name="badge_padding_horizontal">8dp</dimen>
  <dimen name="badge_corner_radius">20dp</dimen>

  <!-- 바텀 내비 -->
  <dimen name="bottom_nav_height">60dp</dimen>

  <!-- Step Indicator -->
  <dimen name="step_node_size">28dp</dimen>
  <dimen name="step_line_height">2dp</dimen>

  <!-- 게이지 바 -->
  <dimen name="progress_bar_height">6dp</dimen>
  <dimen name="sleep_bar_height">8dp</dimen>
  <dimen name="energy_circle_size">64dp</dimen>
  <dimen name="energy_circle_stroke">6dp</dimen>

  <!-- 최소 터치 영역 -->
  <dimen name="min_touch_target">44dp</dimen>
</resources>
```

---

*웨어러블 환자 모니터링 시스템 MVP — Android Style Guide v1.0*  
*2026년 2월 확정*
