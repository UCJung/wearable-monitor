-- ============================================================
-- 수집 항목 정의 마스터 데이터 14건
-- DataInitializer.java가 item_code 중복 확인 후 프로그래밍 방식으로 삽입.
-- 이 파일은 참조/수동 복구 목적으로 보관.
-- ============================================================

INSERT INTO collection_item_definitions
    (item_code, item_name_ko, category, collection_mode, hc_record_type, unit, collection_interval_desc, display_order, is_active)
VALUES
-- 생체신호 (7종)
('HR',           '심박수',         'VITAL_SIGN', 'CONTINUOUS', 'HeartRateRecord',                    'BPM',   '연속',       1, true),
('RESTING_HR',   '안정시 심박수',  'VITAL_SIGN', 'INTERVAL',  'RestingHeartRateRecord',             'BPM',   '일 1회',     2, true),
('HRV',          '심박변이도',     'VITAL_SIGN', 'SLEEP_ONLY','HeartRateVariabilityRmssdRecord',    'ms',    '수면 중',    3, true),
('SPO2',         '산소포화도',     'VITAL_SIGN', 'SLEEP_ONLY','OxygenSaturationRecord',             '%',     '수면 중',    4, true),
('RESPIRATORY',  '호흡수',         'VITAL_SIGN', 'SLEEP_ONLY','RespiratoryRateRecord',              '회/분', '수면 중',    5, true),
('SKIN_TEMP',    '피부 온도',      'VITAL_SIGN', 'SLEEP_ONLY','SkinTemperatureRecord',              '°C',    '수면 중',    6, true),
('STRESS',       '스트레스 지수',  'VITAL_SIGN', 'INTERVAL',  'StressRecord',                       '점',    '30분',       7, true),
-- 활동 (3종)
('STEPS',        '걸음 수',        'ACTIVITY',   'CONTINUOUS','StepsRecord',                        '걸음',  '연속',       8, true),
('CALORIES',     '소모 칼로리',    'ACTIVITY',   'CONTINUOUS','TotalCaloriesBurnedRecord',           'kcal',  '연속',       9, true),
('EXERCISE',     '운동 세션',      'ACTIVITY',   'INTERVAL',  'ExerciseSessionRecord',              '분',    '자동 감지', 10, true),
-- 수면 (3종)
('SLEEP_DURATION','수면 시간',     'SLEEP',      'SLEEP_ONLY','SleepSessionRecord',                 '시간',  '매일 밤',   11, true),
('SLEEP_STAGE',  '수면 단계',      'SLEEP',      'SLEEP_ONLY','SleepStageRecord',                   '-',     '수면 중',   12, true),
('SLEEP_SCORE',  '수면 점수',      'SLEEP',      'SLEEP_ONLY','SleepSessionRecord',                 '점',    '매일 아침', 13, true),
-- AI 종합 (1종)
('ENERGY_SCORE', '에너지 점수',    'AI_SCORE',   'INTERVAL',  NULL,                                 '점',    '매일 아침', 14, true)
ON CONFLICT (item_code) DO NOTHING;
