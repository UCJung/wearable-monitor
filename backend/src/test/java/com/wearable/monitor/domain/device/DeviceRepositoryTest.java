package com.wearable.monitor.domain.device;

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

import java.util.List;
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
class DeviceRepositoryTest {

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
    DeviceRepository deviceRepository;

    @Test
    @DisplayName("기기 저장 및 serialNumber 조회")
    void saveAndFindBySerialNumber() {
        Device device = new Device("SN-001", "Galaxy Watch 7");
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findBySerialNumber("SN-001");
        assertThat(found).isPresent();
        assertThat(found.get().getModelName()).isEqualTo("Galaxy Watch 7");
        assertThat(found.get().getDeviceStatus()).isEqualTo(DeviceStatus.AVAILABLE);
    }

    @Test
    @DisplayName("serialNumber 존재 여부 확인")
    void existsBySerialNumber() {
        deviceRepository.save(new Device("SN-002", "Galaxy Watch 7"));

        assertThat(deviceRepository.existsBySerialNumber("SN-002")).isTrue();
        assertThat(deviceRepository.existsBySerialNumber("SN-999")).isFalse();
    }

    @Test
    @DisplayName("상태별 기기 목록 조회")
    void findByDeviceStatus() {
        deviceRepository.save(new Device("SN-010", "Galaxy Watch 7"));
        Device assigned = new Device("SN-011", "Galaxy Watch 7");
        assigned.changeStatus(DeviceStatus.ASSIGNED);
        deviceRepository.save(assigned);

        List<Device> available = deviceRepository.findByDeviceStatus(DeviceStatus.AVAILABLE);
        List<Device> assignedList = deviceRepository.findByDeviceStatus(DeviceStatus.ASSIGNED);

        assertThat(available).anyMatch(d -> d.getSerialNumber().equals("SN-010"));
        assertThat(assignedList).anyMatch(d -> d.getSerialNumber().equals("SN-011"));
    }
}
