package com.wearable.monitor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("환자 E2E 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientApiIntegrationTest extends IntegrationTestBase {

    @BeforeEach
    void setUp() throws Exception {
        loginAndGetToken();
    }

    @Test
    @Order(1)
    @DisplayName("환자 등록 → 조회 → 장치 등록 → 할당 → 수집 업로드 → 엑셀 다운로드 전 흐름")
    void fullE2eFlow() throws Exception {
        // 1. 환자 등록
        String patientJson = """
                {
                    "name": "통합테스트환자",
                    "birthDate": "1985-06-15",
                    "gender": "M",
                    "notes": "통합테스트 환자"
                }
                """;

        MvcResult patientResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("통합테스트환자"))
                .andReturn();

        JsonNode patientData = objectMapper.readTree(
                patientResult.getResponse().getContentAsString()).get("data");
        Long patientId = patientData.get("id").asLong();
        String patientCode = patientData.get("patientCode").asText();

        assertThat(patientCode).startsWith("PT-");

        // 2. 환자 상세 조회
        mockMvc.perform(get("/api/v1/patients/" + patientId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("통합테스트환자"))
                .andExpect(jsonPath("$.data.patientCode").value(patientCode));

        // 3. 장치 등록
        String deviceJson = """
                {
                    "serialNumber": "SN-INTEG-001",
                    "modelName": "Galaxy Watch 7"
                }
                """;

        MvcResult deviceResult = mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.serialNumber").value("SN-INTEG-001"))
                .andReturn();

        Long deviceId = objectMapper.readTree(
                deviceResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 4. 장치 할당
        String assignJson = String.format("""
                {
                    "patientId": %d,
                    "deviceId": %d,
                    "startDate": "%s"
                }
                """, patientId, deviceId, LocalDate.now());

        mockMvc.perform(post("/api/v1/assignments")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 5. 생체 데이터 배치 업로드
        LocalDateTime now = LocalDateTime.now();
        String batchJson = String.format("""
                {
                    "deviceSerialNumber": "SN-INTEG-001",
                    "items": [
                        {"itemCode": "HR", "measuredAt": "%s", "valueNumeric": 72.0},
                        {"itemCode": "HR", "measuredAt": "%s", "valueNumeric": 75.0},
                        {"itemCode": "SPO2", "measuredAt": "%s", "valueNumeric": 98.0},
                        {"itemCode": "STEPS", "measuredAt": "%s", "valueNumeric": 1500}
                    ]
                }
                """,
                now.minusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                now.minusMinutes(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                now.minusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                now.minusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        mockMvc.perform(post("/api/v1/biometric/batch")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.saved").value(4))
                .andExpect(jsonPath("$.data.skipped").value(0));

        // 6. 수집 이력 조회
        mockMvc.perform(get("/api/v1/biometric/" + patientId)
                        .header("Authorization", bearer())
                        .param("start", LocalDate.now().minusDays(1).toString())
                        .param("end", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(4));

        // 7. 엑셀 다운로드
        MvcResult excelResult = mockMvc.perform(get("/api/v1/export/patient/" + patientId)
                        .header("Authorization", bearer())
                        .param("start", LocalDate.now().minusDays(1).toString())
                        .param("end", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] excelBytes = excelResult.getResponse().getContentAsByteArray();
        assertThat(excelBytes.length).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("환자 목록 조회 (페이징)")
    void patientListPaging() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", bearer())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("인증 없이 API 호출 시 401")
    void unauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isForbidden());
    }
}
