package com.wearable.monitor.domain.patient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import com.wearable.monitor.config.QueryDslConfig;
import com.wearable.monitor.config.JpaAuditingConfig;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Optional;

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
class PatientRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("timescale/timescaledb:latest-pg16")
                            .asCompatibleSubstituteFor("postgres"))
                    .withDatabaseName("wearable_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    PatientRepository patientRepository;

    @Test
    @DisplayName("환자 저장 및 patientCode 조회")
    void saveAndFindByPatientCode() {
        Patient patient = new Patient("PT-0001", "홍길동",
                LocalDate.of(1980, 5, 15), "M", "테스트");

        patientRepository.save(patient);

        Optional<Patient> found = patientRepository.findByPatientCode("PT-0001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
        assertThat(found.get().getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("patientCode 존재 여부 확인")
    void existsByPatientCode() {
        Patient patient = new Patient("PT-0002", "김영희",
                LocalDate.of(1975, 8, 22), "F", null);
        patientRepository.save(patient);

        assertThat(patientRepository.existsByPatientCode("PT-0002")).isTrue();
        assertThat(patientRepository.existsByPatientCode("PT-9999")).isFalse();
    }

    @Test
    @DisplayName("patient_code_seq 시퀀스 nextCode 채번")
    void nextCodeSequence() {
        Long code1 = patientRepository.nextCode();
        Long code2 = patientRepository.nextCode();
        assertThat(code1).isNotNull();
        assertThat(code2).isGreaterThan(code1);
    }

    @Test
    @DisplayName("환자 상태 변경 (소프트 삭제)")
    void changeStatusToDeleted() {
        Patient patient = new Patient("PT-0030", "이철수",
                LocalDate.of(1985, 3, 3), "M", null);
        patientRepository.save(patient);

        patient.changeStatus(PatientStatus.DELETED);
        patientRepository.save(patient);

        Optional<Patient> found = patientRepository.findByPatientCode("PT-0030");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(PatientStatus.DELETED);
    }
}
