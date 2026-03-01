package com.wearable.monitor.common;

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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CollectionItemDefinitionRepository itemDefRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;
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

        // 시퀀스를 삽입된 환자 수에 맞게 조정
        entityManager.createNativeQuery("SELECT setval('patient_code_seq', 3)").getSingleResult();

        insertDeviceIfAbsent("SN-WATCH-001", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-002", "Galaxy Watch 7");
        insertDeviceIfAbsent("SN-WATCH-003", "Galaxy Watch 7");

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
}
