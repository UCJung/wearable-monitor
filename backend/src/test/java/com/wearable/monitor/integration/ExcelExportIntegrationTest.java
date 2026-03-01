package com.wearable.monitor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("엑셀 내보내기 통합 테스트")
class ExcelExportIntegrationTest extends IntegrationTestBase {

    private Long patientId;

    @BeforeEach
    void setupData() throws Exception {
        loginAndGetToken();

        // 환자 등록
        String patientJson = """
                {
                    "name": "엑셀테스트환자",
                    "birthDate": "1988-03-20",
                    "gender": "M",
                    "notes": "엑셀 테스트"
                }
                """;

        MvcResult patientResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isCreated())
                .andReturn();

        patientId = objectMapper.readTree(
                patientResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 장치 등록
        String deviceSerial = "SN-EXCEL-" + System.currentTimeMillis();
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

        // 대량 데이터 업로드 (200건씩 5회 = 1000건)
        String[] itemCodes = {"HR", "SPO2", "STEPS", "CALORIES", "SKIN_TEMP"};
        LocalDateTime base = LocalDateTime.now().minusDays(5);

        for (int batch = 0; batch < 5; batch++) {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                String measuredAt = base.plusMinutes((long) batch * 200 + i)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String code = itemCodes[i % itemCodes.length];
                items.add(String.format(
                        """
                        {"itemCode": "%s", "measuredAt": "%s", "valueNumeric": %s}""",
                        code, measuredAt, 60 + (i % 40)));
            }

            String batchJson = String.format("""
                    {"deviceSerialNumber": "%s", "items": [%s]}
                    """, deviceSerial, String.join(",", items));

            mockMvc.perform(post("/api/v1/biometric/batch")
                            .header("Authorization", bearer())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(batchJson))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("환자별 엑셀 내보내기 — 시트 2종 및 컬럼 수 검증")
    void exportPatientExcel_sheetsAndColumns() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/export/patient/" + patientId)
                        .header("Authorization", bearer())
                        .param("start", LocalDate.now().minusDays(10).toString())
                        .param("end", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andReturn();

        byte[] excelBytes = result.getResponse().getContentAsByteArray();
        assertThat(excelBytes.length).isGreaterThan(0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            // 2개 시트: 수집이력, 일별요약
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);

            XSSFSheet historySheet = workbook.getSheetAt(0);
            assertThat(historySheet.getSheetName()).contains("수집이력");
            // 헤더 행 컬럼 수: 10
            assertThat(historySheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(10);
            // 데이터 행이 있어야 함
            assertThat(historySheet.getPhysicalNumberOfRows()).isGreaterThan(1);

            XSSFSheet summarySheet = workbook.getSheetAt(1);
            assertThat(summarySheet.getSheetName()).contains("일별요약");
            // 헤더 행 컬럼 수: 7
            assertThat(summarySheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(7);
        }
    }

    @Test
    @DisplayName("전체 엑셀 내보내기 — 시트 4종 검증")
    void exportAllExcel_fourSheets() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/export/all")
                        .header("Authorization", bearer())
                        .param("start", LocalDate.now().minusDays(10).toString())
                        .param("end", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andReturn();

        byte[] excelBytes = result.getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(workbook.getSheetAt(0).getSheetName()).contains("환자");
            assertThat(workbook.getSheetAt(1).getSheetName()).contains("수집이력");
            assertThat(workbook.getSheetAt(2).getSheetName()).contains("일별요약");
            assertThat(workbook.getSheetAt(3).getSheetName()).contains("기기");
        }
    }

    @Test
    @DisplayName("엑셀 생성 성능 테스트 (1000건 기준 5초 이내)")
    void exportExcel_performance() throws Exception {
        long start = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/export/patient/" + patientId)
                        .header("Authorization", bearer())
                        .param("start", LocalDate.now().minusDays(10).toString())
                        .param("end", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk());

        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).as("엑셀 생성은 5초 이내 완료").isLessThan(5000);
    }
}
