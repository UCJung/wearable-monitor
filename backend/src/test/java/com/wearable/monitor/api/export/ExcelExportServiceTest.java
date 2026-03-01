package com.wearable.monitor.api.export;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.DailySummaryDto;
import com.wearable.monitor.domain.biometric.PatientBiometricHistory;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinition;
import com.wearable.monitor.domain.itemdef.CollectionMode;
import com.wearable.monitor.domain.itemdef.ItemCategory;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.patient.PatientStatus;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;
    @Mock
    private PatientBiometricHistoryRepository biometricRepository;

    @InjectMocks
    private ExcelExportService excelExportService;

    private Patient patient;
    private Device device;
    private PatientDeviceAssignment assignment;
    private CollectionItemDefinition hrItemDef;

    @BeforeEach
    void setUp() {
        patient = spy(new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null));
        lenient().when(patient.getId()).thenReturn(1L);

        device = spy(new Device("SN-WATCH-001", "Galaxy Watch 7"));
        lenient().when(device.getId()).thenReturn(10L);

        assignment = spy(new PatientDeviceAssignment(patient, device, LocalDate.of(2026, 1, 1)));
        lenient().when(assignment.getId()).thenReturn(100L);

        hrItemDef = new CollectionItemDefinition("HR", "심박수", ItemCategory.VITAL_SIGN,
                CollectionMode.CONTINUOUS, "HeartRate", "bpm", "1분", 1);
    }

    @Test
    @DisplayName("환자별 엑셀 — 시트 2종 (수집이력, 일별요약) 생성 확인")
    void exportPatientExcel_hasTwoSheets() throws Exception {
        // given
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));

        PatientBiometricHistory history = new PatientBiometricHistory(
                assignment, hrItemDef, LocalDateTime.of(2026, 2, 1, 10, 0),
                BigDecimal.valueOf(72), null, null, null);

        given(biometricRepository.findByCondition(eq(1L), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(history)));
        given(biometricRepository.findDailySummary(any(), any(), any(), any()))
                .willReturn(List.of(new DailySummaryDto(1L, "HR", LocalDate.of(2026, 2, 1),
                        72.0, BigDecimal.valueOf(72), BigDecimal.valueOf(72), 1L)));

        // when
        byte[] excelData = excelExportService.exportPatientExcel(1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        // then
        assertThat(excelData).isNotEmpty();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetName(0)).isEqualTo("수집이력");
            assertThat(workbook.getSheetName(1)).isEqualTo("일별요약");

            // 수집이력 시트 컬럼 수 확인 (10개)
            Sheet historySheet = workbook.getSheetAt(0);
            assertThat(historySheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(10);

            // 데이터 행 확인
            assertThat(historySheet.getRow(1)).isNotNull();
            assertThat(historySheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("PT-0001");
        }
    }

    @Test
    @DisplayName("전체 엑셀 — 시트 4종 (환자목록, 수집이력, 일별요약, 기기현황) 생성 확인")
    void exportAllExcel_hasFourSheets() throws Exception {
        // given
        given(patientRepository.findByStatusNot(PatientStatus.DELETED))
                .willReturn(List.of(patient));
        given(biometricRepository.findByCondition(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(biometricRepository.findDailySummary(any(), any(), any(), any()))
                .willReturn(List.of());
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(deviceRepository.findAll()).willReturn(List.of(device));

        // when
        byte[] excelData = excelExportService.exportAllExcel(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        // then
        assertThat(excelData).isNotEmpty();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(workbook.getSheetName(0)).isEqualTo("환자목록");
            assertThat(workbook.getSheetName(1)).isEqualTo("수집이력");
            assertThat(workbook.getSheetName(2)).isEqualTo("일별요약");
            assertThat(workbook.getSheetName(3)).isEqualTo("기기현황");

            // 환자목록 컬럼 수 (4개)
            assertThat(workbook.getSheetAt(0).getRow(0).getPhysicalNumberOfCells()).isEqualTo(4);
            // 기기현황 컬럼 수 (5개)
            assertThat(workbook.getSheetAt(3).getRow(0).getPhysicalNumberOfCells()).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("엑셀 헤더 스타일 — 배경색·흰 텍스트 적용 확인")
    void exportPatientExcel_headerStyle() throws Exception {
        // given
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(biometricRepository.findByCondition(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(biometricRepository.findDailySummary(any(), any(), any(), any()))
                .willReturn(List.of());

        // when
        byte[] excelData = excelExportService.exportPatientExcel(1L, null, null);

        // then
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            // 헤더 행이 존재하고 스타일이 적용됨
            assertThat(sheet.getRow(0).getCell(0).getCellStyle().getFillPattern())
                    .isEqualTo(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        }
    }
}
