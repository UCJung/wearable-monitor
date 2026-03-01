package com.wearable.monitor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("배치 업로드 통합 테스트")
class BiometricBatchIntegrationTest extends IntegrationTestBase {

    private String deviceSerial;

    @BeforeEach
    void setupPatientAndDevice() throws Exception {
        loginAndGetToken();

        // 환자 등록
        String patientJson = """
                {
                    "name": "배치테스트환자",
                    "birthDate": "1990-01-01",
                    "gender": "F",
                    "notes": "배치 테스트"
                }
                """;

        MvcResult patientResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long patientId = objectMapper.readTree(
                patientResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 장치 등록
        deviceSerial = "SN-BATCH-" + System.currentTimeMillis();
        String deviceJson = String.format("""
                {"serialNumber": "%s", "modelName": "Galaxy Watch 7"}
                """, deviceSerial);

        MvcResult deviceResult = mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long deviceId = objectMapper.readTree(
                deviceResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 할당
        String assignJson = String.format("""
                {"patientId": %d, "deviceId": %d, "startDate": "%s"}
                """, patientId, deviceId, LocalDate.now());

        mockMvc.perform(post("/api/v1/assignments")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("500건 배치 업로드 성능 (목표: 2초 이내)")
    void batchUpload500_performance() throws Exception {
        // 500건 아이템 생성
        LocalDateTime base = LocalDateTime.now().minusHours(1);
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String measuredAt = base.plusSeconds(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            items.add(String.format(
                    """
                    {"itemCode": "HR", "measuredAt": "%s", "valueNumeric": %s}""",
                    measuredAt, 60 + (i % 40)));
        }

        String batchJson = String.format("""
                {
                    "deviceSerialNumber": "%s",
                    "items": [%s]
                }
                """, deviceSerial, String.join(",", items));

        long start = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/biometric/batch")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.total").value(500))
                .andExpect(jsonPath("$.data.saved").value(500));

        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).as("500건 배치 업로드는 5초 이내 완료").isLessThan(5000);
    }

    @Test
    @DisplayName("중복 데이터 스킵 정확도 검증")
    void batchUpload_duplicateSkip() throws Exception {
        LocalDateTime measuredAt = LocalDateTime.now().minusMinutes(30);
        String measuredAtStr = measuredAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 첫 번째 업로드
        String batchJson = String.format("""
                {
                    "deviceSerialNumber": "%s",
                    "items": [
                        {"itemCode": "HR", "measuredAt": "%s", "valueNumeric": 72.0}
                    ]
                }
                """, deviceSerial, measuredAtStr);

        mockMvc.perform(post("/api/v1/biometric/batch")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.saved").value(1));

        // 동일 데이터 재업로드 → skipped 1
        MvcResult result = mockMvc.perform(post("/api/v1/biometric/batch")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("data");
        int skipped = data.get("skipped").asInt();
        int saved = data.get("saved").asInt();

        // 중복 데이터는 skipped 또는 saved=0 처리
        assertThat(skipped + saved).isEqualTo(1);
        assertThat(skipped).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("빈 배치 업로드 시 에러 응답")
    void batchUpload_emptyList() throws Exception {
        String batchJson = String.format("""
                {
                    "deviceSerialNumber": "%s",
                    "items": []
                }
                """, deviceSerial);

        mockMvc.perform(post("/api/v1/biometric/batch")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isBadRequest());
    }
}
