package com.wearable.monitor.domain.biometric;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinition;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinitionRepository;
import com.wearable.monitor.domain.itemdef.CollectionMode;
import com.wearable.monitor.domain.itemdef.ItemCategory;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import com.wearable.monitor.config.QueryDslConfig;
import com.wearable.monitor.config.JpaAuditingConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
@Testcontainers
@Sql(scripts = {"/sql/schema.sql", "/sql/timescaledb.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PatientBiometricHistoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("timescale/timescaledb:latest-pg16")
                            .asCompatibleSubstituteFor("postgres"))
                    .withDatabaseName("wearable_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired PatientBiometricHistoryRepository biometricRepository;
    @Autowired PatientRepository patientRepository;
    @Autowired DeviceRepository deviceRepository;
    @Autowired PatientDeviceAssignmentRepository assignmentRepository;
    @Autowired CollectionItemDefinitionRepository itemDefRepository;

    PatientDeviceAssignment assignment;
    CollectionItemDefinition hrItemDef;

    @BeforeEach
    void setUp() {
        biometricRepository.deleteAll();
        assignmentRepository.deleteAll();
        patientRepository.deleteAll();
        deviceRepository.deleteAll();
        itemDefRepository.deleteAll();

        Patient patient = patientRepository.save(
                new Patient("PT-0001", "홍길동", LocalDate.of(1980, 5, 15), "M", null));
        Device device = deviceRepository.save(
                new Device("SN-WATCH-001", "Galaxy Watch 7"));
        assignment = assignmentRepository.save(
                new PatientDeviceAssignment(patient, device, LocalDate.now()));
        hrItemDef = itemDefRepository.save(
                new CollectionItemDefinition("HR", "심박수", ItemCategory.VITAL_SIGN,
                        CollectionMode.CONTINUOUS, "HeartRateRecord", "BPM", "연속", 1));
    }

    @Test
    @DisplayName("생체신호 저장 및 INSERT 확인")
    void saveAndCount() {
        PatientBiometricHistory history = new PatientBiometricHistory(
                assignment, hrItemDef,
                LocalDateTime.now(),
                BigDecimal.valueOf(75.0),
                null, null, null);

        biometricRepository.save(history);

        assertThat(biometricRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("QueryDSL — 환자별 조건 조회 (findByCondition)")
    void findByCondition() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            biometricRepository.save(new PatientBiometricHistory(
                    assignment, hrItemDef,
                    now.minusMinutes(i),
                    BigDecimal.valueOf(70 + i),
                    null, null, null));
        }

        Page<PatientBiometricHistory> result = biometricRepository.findByCondition(
                assignment.getPatient().getId(),
                List.of("HR"),
                now.minusHours(1),
                now.plusHours(1),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).allMatch(h -> h.getItemCode().equals("HR"));
    }

    @Test
    @DisplayName("QueryDSL — 일별 요약 조회 (findDailySummary)")
    void findDailySummary() {
        LocalDateTime today = LocalDate.now().atTime(10, 0);

        for (int i = 0; i < 3; i++) {
            biometricRepository.save(new PatientBiometricHistory(
                    assignment, hrItemDef,
                    today.plusMinutes(i * 10),
                    BigDecimal.valueOf(70 + i * 5),
                    null, null, null));
        }

        List<DailySummaryDto> summary = biometricRepository.findDailySummary(
                List.of(assignment.getPatient().getId()),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1));

        assertThat(summary).isNotEmpty();
        DailySummaryDto dto = summary.get(0);
        assertThat(dto.getItemCode()).isEqualTo("HR");
        assertThat(dto.getMeasureCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("할당 상태 변경 관계 확인")
    void assignmentStatusChange() {
        assignment.returnDevice(LocalDate.now());
        assignmentRepository.save(assignment);

        PatientDeviceAssignment found = assignmentRepository
                .findById(assignment.getId()).orElseThrow();
        assertThat(found.getAssignmentStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(found.getReturnedAt()).isNotNull();
    }
}
