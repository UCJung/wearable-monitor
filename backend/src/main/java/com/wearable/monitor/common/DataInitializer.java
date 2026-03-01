package com.wearable.monitor.common;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.PatientBiometricHistory;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.itemdef.*;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.user.User;
import com.wearable.monitor.domain.user.UserRepository;
import com.wearable.monitor.domain.user.UserRole;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CollectionItemDefinitionRepository itemDefRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final PatientBiometricHistoryRepository biometricRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        insertMasterData();
        insertAdminUser();
        if (isDevProfile()) {
            insertDevData();
        }
    }

    private void insertMasterData() {
        log.info("[DataInitializer] 수집 항목 마스터 데이터 초기화 시작");

        List<Object[]> items = List.of(
            new Object[]{"HR",            "심박수",         ItemCategory.VITAL_SIGN, CollectionMode.CONTINUOUS, "HeartRateRecord",                  "BPM",   "연속",        1},
            new Object[]{"RESTING_HR",    "안정시 심박수",  ItemCategory.VITAL_SIGN, CollectionMode.INTERVAL,   "RestingHeartRateRecord",           "BPM",   "일 1회",      2},
            new Object[]{"HRV",           "심박변이도",     ItemCategory.VITAL_SIGN, CollectionMode.SLEEP_ONLY, "HeartRateVariabilityRmssdRecord",  "ms",    "수면 중",     3},
            new Object[]{"SPO2",          "산소포화도",     ItemCategory.VITAL_SIGN, CollectionMode.SLEEP_ONLY, "OxygenSaturationRecord",           "%",     "수면 중",     4},
            new Object[]{"RESPIRATORY",   "호흡수",         ItemCategory.VITAL_SIGN, CollectionMode.SLEEP_ONLY, "RespiratoryRateRecord",            "회/분", "수면 중",     5},
            new Object[]{"SKIN_TEMP",     "피부 온도",      ItemCategory.VITAL_SIGN, CollectionMode.SLEEP_ONLY, "SkinTemperatureRecord",            "°C",    "수면 중",     6},
            new Object[]{"STRESS",        "스트레스 지수",  ItemCategory.VITAL_SIGN, CollectionMode.INTERVAL,   "StressRecord",                     "점",    "30분",        7},
            new Object[]{"STEPS",         "걸음 수",        ItemCategory.ACTIVITY,   CollectionMode.CONTINUOUS, "StepsRecord",                      "걸음",  "연속",        8},
            new Object[]{"CALORIES",      "소모 칼로리",    ItemCategory.ACTIVITY,   CollectionMode.CONTINUOUS, "TotalCaloriesBurnedRecord",        "kcal",  "연속",        9},
            new Object[]{"EXERCISE",      "운동 세션",      ItemCategory.ACTIVITY,   CollectionMode.INTERVAL,   "ExerciseSessionRecord",            "분",    "자동 감지",  10},
            new Object[]{"SLEEP_DURATION","수면 시간",      ItemCategory.SLEEP,      CollectionMode.SLEEP_ONLY, "SleepSessionRecord",               "시간",  "매일 밤",    11},
            new Object[]{"SLEEP_STAGE",   "수면 단계",      ItemCategory.SLEEP,      CollectionMode.SLEEP_ONLY, "SleepStageRecord",                 "-",     "수면 중",    12},
            new Object[]{"SLEEP_SCORE",   "수면 점수",      ItemCategory.SLEEP,      CollectionMode.SLEEP_ONLY, "SleepSessionRecord",               "점",    "매일 아침",  13},
            new Object[]{"ENERGY_SCORE",  "에너지 점수",    ItemCategory.AI_SCORE,   CollectionMode.INTERVAL,   null,                               "점",    "매일 아침",  14}
        );

        int inserted = 0;
        for (Object[] item : items) {
            String itemCode = (String) item[0];
            if (itemDefRepository.findByItemCode(itemCode).isEmpty()) {
                itemDefRepository.save(new CollectionItemDefinition(
                    itemCode,
                    (String) item[1],
                    (ItemCategory) item[2],
                    (CollectionMode) item[3],
                    (String) item[4],
                    (String) item[5],
                    (String) item[6],
                    (Integer) item[7]
                ));
                inserted++;
            }
        }
        log.info("[DataInitializer] 수집 항목 {}건 신규 삽입 (기존 건너뜀)", inserted);
    }

    private void insertAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User("admin", passwordEncoder.encode("admin1234"), UserRole.STAFF));
            log.info("[DataInitializer] 기본 관리자 계정 생성: admin");
        }
    }

    private void insertDevData() {
        log.info("[DataInitializer] 개발 환경 테스트 데이터 초기화 시작");

        insertPatientIfAbsent("PT-0001", "홍길동", LocalDate.of(1980, 5, 15), "M", "테스트 환자 1");
        insertPatientIfAbsent("PT-0002", "김영희", LocalDate.of(1975, 8, 22), "F", "테스트 환자 2");
        insertPatientIfAbsent("PT-0003", "이철수", LocalDate.of(1990, 3, 10), "M", "테스트 환자 3");
        insertPatientIfAbsent("PT-0004", "박지영", LocalDate.of(1985, 11, 3), "F", "테스트 환자 4");
        insertPatientIfAbsent("PT-0005", "최민수", LocalDate.of(1992, 7, 20), "M", "테스트 환자 5");

        // 시퀀스를 삽입된 환자 수에 맞게 조정
        entityManager.createNativeQuery("SELECT setval('patient_code_seq', 5)").getSingleResult();

        insertDeviceIfAbsent("SN-WATCH-001", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-002", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-003", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-004", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-005", "Galaxy Watch 7");

        // 환자-장치 자동 할당 (번호 매칭)
        LocalDate assignStartDate = LocalDate.now().minusDays(7);
        for (int i = 1; i <= 5; i++) {
            String patientCode = String.format("PT-%04d", i);
            String serialNumber = String.format("SN-WATCH-%03d", i);
            assignDeviceIfAbsent(patientCode, serialNumber, assignStartDate);
        }

        // PT-0001, PT-0002에 대해 벌크 생체신호 데이터 생성
        generateBulkBiometricData(List.of("PT-0001", "PT-0002"), 7);

        log.info("[DataInitializer] 개발 환경 테스트 데이터 초기화 완료");
    }

    private void insertPatientIfAbsent(String code, String name, LocalDate birthDate,
                                        String gender, String notes) {
        if (patientRepository.findByPatientCode(code).isEmpty()) {
            Patient patient = new Patient(code, name, birthDate, gender, notes);
            patientRepository.save(patient);
            entityManager.flush();
            log.info("[DataInitializer] 테스트 환자 삽입: {}", code);

            // 환자 계정 자동 생성
            if (!userRepository.existsByUsername(code)) {
                User patientUser = User.forPatient(code, passwordEncoder.encode(code + "1!"), patient.getId());
                userRepository.save(patientUser);
                log.info("[DataInitializer] 환자 계정 생성: {}", code);
            }
        } else {
            // 기존 환자가 있지만 계정이 없는 경우 생성
            patientRepository.findByPatientCode(code).ifPresent(patient -> {
                if (!userRepository.existsByUsername(code)) {
                    User patientUser = User.forPatient(code, passwordEncoder.encode(code + "1!"), patient.getId());
                    userRepository.save(patientUser);
                    log.info("[DataInitializer] 기존 환자 계정 생성: {}", code);
                }
            });
        }
    }

    private void insertDeviceIfAbsent(String serial, String modelName) {
        if (deviceRepository.findBySerialNumber(serial).isEmpty()) {
            deviceRepository.save(new Device(serial, modelName));
            log.info("[DataInitializer] 테스트 기기 삽입: {}", serial);
        }
    }

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private void assignDeviceIfAbsent(String patientCode, String serialNumber, LocalDate startDate) {
        Patient patient = patientRepository.findByPatientCode(patientCode).orElse(null);
        Device device = deviceRepository.findBySerialNumber(serialNumber).orElse(null);
        if (patient == null || device == null) return;

        Optional<PatientDeviceAssignment> existing =
                assignmentRepository.findByPatientIdAndAssignmentStatus(
                        patient.getId(), AssignmentStatus.ACTIVE);
        if (existing.isPresent()) return;

        PatientDeviceAssignment assignment = new PatientDeviceAssignment(patient, device, startDate);
        assignmentRepository.save(assignment);
        entityManager.flush();
        log.info("[DataInitializer] 자동 할당: {} ↔ {}", patientCode, serialNumber);
    }

    private void generateBulkBiometricData(List<String> patientCodes, int days) {
        // 이미 데이터가 있으면 스킵
        long existingCount = biometricRepository.count();
        if (existingCount > 0) {
            log.info("[DataInitializer] 생체신호 데이터 이미 존재 ({}건), 벌크 생성 스킵", existingCount);
            return;
        }

        Random random = new Random(42); // 재현 가능한 시드
        Map<String, CollectionItemDefinition> itemDefs = new HashMap<>();
        itemDefRepository.findAll().forEach(def -> itemDefs.put(def.getItemCode(), def));

        LocalDate today = LocalDate.now();
        List<PatientBiometricHistory> allRecords = new ArrayList<>();

        for (String patientCode : patientCodes) {
            Patient patient = patientRepository.findByPatientCode(patientCode).orElse(null);
            if (patient == null) continue;

            Optional<PatientDeviceAssignment> assignmentOpt =
                    assignmentRepository.findByPatientIdAndAssignmentStatus(
                            patient.getId(), AssignmentStatus.ACTIVE);
            if (assignmentOpt.isEmpty()) continue;

            PatientDeviceAssignment assignment = assignmentOpt.get();

            for (int dayOffset = days - 1; dayOffset >= 0; dayOffset--) {
                LocalDate date = today.minusDays(dayOffset);
                generateDayData(random, assignment, itemDefs, date, allRecords);
            }
        }

        // 배치 저장 (500건씩)
        int batchSize = 500;
        for (int i = 0; i < allRecords.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allRecords.size());
            biometricRepository.saveAll(allRecords.subList(i, end));
            entityManager.flush();
            entityManager.clear();
        }

        log.info("[DataInitializer] 벌크 생체신호 데이터 생성 완료: {}건", allRecords.size());
    }

    private void generateDayData(Random random, PatientDeviceAssignment assignment,
                                  Map<String, CollectionItemDefinition> itemDefs,
                                  LocalDate date, List<PatientBiometricHistory> records) {
        // 30분 간격 연속 항목 (HR, STRESS, STEPS, CALORIES)
        for (int slot = 0; slot < 48; slot++) {
            LocalDateTime ts = LocalDateTime.of(date, LocalTime.of(slot / 2, (slot % 2) * 30));
            int hour = slot / 2;
            boolean isSleepHour = hour < 7 || hour >= 23;

            // HR: 60~100 BPM
            addRecord(records, assignment, itemDefs.get("HR"), ts,
                    randomBigDecimal(random, 60, 100), null, null, null);

            // STRESS: 20~80
            addRecord(records, assignment, itemDefs.get("STRESS"), ts,
                    randomBigDecimal(random, 20, 80), null, null, null);

            // STEPS: 수면시간 0, 활동시간 0~500
            int steps = isSleepHour ? 0 : random.nextInt(501);
            addRecord(records, assignment, itemDefs.get("STEPS"), ts,
                    BigDecimal.valueOf(steps), null, null, null);

            // CALORIES: 수면시간 낮은 값, 활동시간 0~50
            int cal = isSleepHour ? random.nextInt(10) : random.nextInt(51);
            addRecord(records, assignment, itemDefs.get("CALORIES"), ts,
                    BigDecimal.valueOf(cal), null, null, null);
        }

        // 수면 시간대 항목 (00:00~06:00, 30분 간격 = 12슬롯)
        for (int slot = 0; slot < 12; slot++) {
            LocalDateTime ts = LocalDateTime.of(date, LocalTime.of(slot / 2, (slot % 2) * 30));

            // HRV: 20~80 ms
            addRecord(records, assignment, itemDefs.get("HRV"), ts,
                    randomBigDecimal(random, 20, 80), null, null, null);

            // SPO2: 94~100 %
            addRecord(records, assignment, itemDefs.get("SPO2"), ts,
                    randomBigDecimal(random, 94, 100), null, null, null);

            // RESPIRATORY: 12~20 회/분
            addRecord(records, assignment, itemDefs.get("RESPIRATORY"), ts,
                    randomBigDecimal(random, 12, 20), null, null, null);

            // SKIN_TEMP: 35.5~37.0 °C
            double temp = 35.5 + random.nextDouble() * 1.5;
            addRecord(records, assignment, itemDefs.get("SKIN_TEMP"), ts,
                    BigDecimal.valueOf(temp).setScale(1, java.math.RoundingMode.HALF_UP),
                    null, null, null);
        }

        // 하루 1건 항목들
        // RESTING_HR: 08:00, 55~75 BPM
        addRecord(records, assignment, itemDefs.get("RESTING_HR"),
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                randomBigDecimal(random, 55, 75), null, null, null);

        // EXERCISE: 18:00, 0~60분 (확률 70%)
        if (random.nextDouble() < 0.7) {
            int exerciseMin = 15 + random.nextInt(46);
            addRecord(records, assignment, itemDefs.get("EXERCISE"),
                    LocalDateTime.of(date, LocalTime.of(18, 0)),
                    BigDecimal.valueOf(exerciseMin), null, null, exerciseMin * 60);
        }

        // SLEEP_DURATION: 07:00, 360~480분
        int sleepMin = 360 + random.nextInt(121);
        addRecord(records, assignment, itemDefs.get("SLEEP_DURATION"),
                LocalDateTime.of(date, LocalTime.of(7, 0)),
                BigDecimal.valueOf(sleepMin), null, null, sleepMin * 60);

        // SLEEP_STAGE: 07:00, JSON
        String sleepStageJson = generateSleepStageJson(random, sleepMin);
        addRecord(records, assignment, itemDefs.get("SLEEP_STAGE"),
                LocalDateTime.of(date, LocalTime.of(7, 0)),
                null, null, sleepStageJson, sleepMin * 60);

        // SLEEP_SCORE: 07:00, 60~95
        addRecord(records, assignment, itemDefs.get("SLEEP_SCORE"),
                LocalDateTime.of(date, LocalTime.of(7, 0)),
                randomBigDecimal(random, 60, 95), null, null, null);

        // ENERGY_SCORE: 09:00, 50~95
        addRecord(records, assignment, itemDefs.get("ENERGY_SCORE"),
                LocalDateTime.of(date, LocalTime.of(9, 0)),
                randomBigDecimal(random, 50, 95), null, null, null);
    }

    private void addRecord(List<PatientBiometricHistory> records,
                            PatientDeviceAssignment assignment, CollectionItemDefinition itemDef,
                            LocalDateTime measuredAt, BigDecimal valueNumeric,
                            String valueText, String valueJson, Integer durationSec) {
        if (itemDef == null) return;
        records.add(new PatientBiometricHistory(
                assignment, itemDef, measuredAt, valueNumeric, valueText, valueJson, durationSec));
    }

    private BigDecimal randomBigDecimal(Random random, int min, int max) {
        return BigDecimal.valueOf(min + random.nextInt(max - min + 1));
    }

    private String generateSleepStageJson(Random random, int totalMinutes) {
        int awake = (int) (totalMinutes * (0.05 + random.nextDouble() * 0.05));
        int rem = (int) (totalMinutes * (0.20 + random.nextDouble() * 0.05));
        int light = (int) (totalMinutes * (0.40 + random.nextDouble() * 0.10));
        int deep = totalMinutes - awake - rem - light;
        return String.format("{\"awake\":%d,\"rem\":%d,\"light\":%d,\"deep\":%d}", awake, rem, light, deep);
    }
}
