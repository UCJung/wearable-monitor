package com.wearable.monitor.api.export;

import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.DailySummaryDto;
import com.wearable.monitor.domain.biometric.PatientBiometricHistory;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.patient.PatientStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final PatientBiometricHistoryRepository biometricRepository;

    @Transactional(readOnly = true)
    public byte[] exportPatientExcel(Long patientId, LocalDate start, LocalDate end) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new WearableException(ErrorCode.PATIENT_NOT_FOUND));

        LocalDateTime startDt = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDt = end != null ? end.atTime(23, 59, 59) : null;

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Sheet1: 수집이력
            createHistorySheet(workbook, headerStyle, patient, startDt, endDt);

            // Sheet2: 일별요약
            createDailySummarySheet(workbook, headerStyle, List.of(patientId), start, end);

            return toByteArray(workbook);
        } catch (IOException e) {
            log.error("Failed to generate patient excel", e);
            throw new WearableException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportAllExcel(LocalDate start, LocalDate end) {
        List<Patient> patients = patientRepository.findByStatusNot(PatientStatus.DELETED);
        List<Long> patientIds = patients.stream().map(Patient::getId).toList();

        LocalDateTime startDt = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDt = end != null ? end.atTime(23, 59, 59) : null;

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Sheet1: 환자목록
            createPatientListSheet(workbook, headerStyle, patients);

            // Sheet2: 수집이력 전체
            createAllHistorySheet(workbook, headerStyle, patients, startDt, endDt);

            // Sheet3: 일별요약
            createDailySummarySheet(workbook, headerStyle, patientIds, start, end);

            // Sheet4: 기기현황
            createDeviceStatusSheet(workbook, headerStyle);

            return toByteArray(workbook);
        } catch (IOException e) {
            log.error("Failed to generate all-patients excel", e);
            throw new WearableException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void createHistorySheet(SXSSFWorkbook workbook, CellStyle headerStyle,
                                     Patient patient, LocalDateTime startDt, LocalDateTime endDt) {
        Sheet sheet = workbook.createSheet("수집이력");
        String[] headers = {"환자 코드", "환자명", "측정 일시", "항목 코드", "항목명",
                "분류", "측정값", "단위", "지속 시간", "장치 시리얼"};
        createHeaderRow(sheet, headerStyle, headers);

        List<PatientBiometricHistory> histories = biometricRepository
                .findByCondition(patient.getId(), null, startDt, endDt,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        int rowNum = 1;
        for (PatientBiometricHistory h : histories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(patient.getPatientCode());
            row.createCell(1).setCellValue(patient.getName());
            row.createCell(2).setCellValue(h.getMeasuredAt().format(DT_FORMAT));
            row.createCell(3).setCellValue(h.getItemCode());
            row.createCell(4).setCellValue(h.getItemDef().getItemNameKo());
            row.createCell(5).setCellValue(categoryToKorean(h.getItemDef().getCategory().name()));
            row.createCell(6).setCellValue(formatValue(h.getValueNumeric(), h.getValueText()));
            row.createCell(7).setCellValue(h.getItemDef().getUnit() != null ? h.getItemDef().getUnit() : "");
            row.createCell(8).setCellValue(formatDuration(h.getDurationSec()));
            row.createCell(9).setCellValue(h.getAssignment().getDevice().getSerialNumber());
        }
    }

    private void createAllHistorySheet(SXSSFWorkbook workbook, CellStyle headerStyle,
                                        List<Patient> patients, LocalDateTime startDt, LocalDateTime endDt) {
        Sheet sheet = workbook.createSheet("수집이력");
        String[] headers = {"환자 코드", "환자명", "측정 일시", "항목 코드", "항목명",
                "분류", "측정값", "단위", "지속 시간", "장치 시리얼"};
        createHeaderRow(sheet, headerStyle, headers);

        int rowNum = 1;
        for (Patient patient : patients) {
            List<PatientBiometricHistory> histories = biometricRepository
                    .findByCondition(patient.getId(), null, startDt, endDt,
                            org.springframework.data.domain.Pageable.unpaged())
                    .getContent();

            for (PatientBiometricHistory h : histories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(patient.getPatientCode());
                row.createCell(1).setCellValue(patient.getName());
                row.createCell(2).setCellValue(h.getMeasuredAt().format(DT_FORMAT));
                row.createCell(3).setCellValue(h.getItemCode());
                row.createCell(4).setCellValue(h.getItemDef().getItemNameKo());
                row.createCell(5).setCellValue(categoryToKorean(h.getItemDef().getCategory().name()));
                row.createCell(6).setCellValue(formatValue(h.getValueNumeric(), h.getValueText()));
                row.createCell(7).setCellValue(h.getItemDef().getUnit() != null ? h.getItemDef().getUnit() : "");
                row.createCell(8).setCellValue(formatDuration(h.getDurationSec()));
                row.createCell(9).setCellValue(h.getAssignment().getDevice().getSerialNumber());
            }
        }
    }

    private void createDailySummarySheet(SXSSFWorkbook workbook, CellStyle headerStyle,
                                          List<Long> patientIds, LocalDate start, LocalDate end) {
        Sheet sheet = workbook.createSheet("일별요약");
        String[] headers = {"환자 ID", "항목 코드", "측정일", "평균값", "최소값", "최대값", "건수"};
        createHeaderRow(sheet, headerStyle, headers);

        List<DailySummaryDto> summaries = biometricRepository.findDailySummary(
                patientIds, null, start, end);

        int rowNum = 1;
        for (DailySummaryDto s : summaries) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(s.getPatientId());
            row.createCell(1).setCellValue(s.getItemCode());
            row.createCell(2).setCellValue(s.getMeasureDate().format(DATE_FORMAT));
            row.createCell(3).setCellValue(s.getAvgValue() != null ? s.getAvgValue() : 0);
            row.createCell(4).setCellValue(s.getMinValue() != null ? s.getMinValue().doubleValue() : 0);
            row.createCell(5).setCellValue(s.getMaxValue() != null ? s.getMaxValue().doubleValue() : 0);
            row.createCell(6).setCellValue(s.getMeasureCount() != null ? s.getMeasureCount() : 0);
        }
    }

    private void createPatientListSheet(SXSSFWorkbook workbook, CellStyle headerStyle,
                                         List<Patient> patients) {
        Sheet sheet = workbook.createSheet("환자목록");
        String[] headers = {"환자 코드", "이름", "상태", "할당 장치"};
        createHeaderRow(sheet, headerStyle, headers);

        int rowNum = 1;
        for (Patient patient : patients) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(patient.getPatientCode());
            row.createCell(1).setCellValue(patient.getName());
            row.createCell(2).setCellValue(patient.getStatus().name());

            Optional<PatientDeviceAssignment> active = assignmentRepository
                    .findByPatientIdAndAssignmentStatus(patient.getId(), AssignmentStatus.ACTIVE);
            row.createCell(3).setCellValue(
                    active.map(a -> a.getDevice().getSerialNumber()).orElse("-"));
        }
    }

    private void createDeviceStatusSheet(SXSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("기기현황");
        String[] headers = {"시리얼 번호", "모델명", "상태", "배터리(%)", "최종 동기화"};
        createHeaderRow(sheet, headerStyle, headers);

        List<Device> devices = deviceRepository.findAll();
        int rowNum = 1;
        for (Device device : devices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(device.getSerialNumber());
            row.createCell(1).setCellValue(device.getModelName() != null ? device.getModelName() : "");
            row.createCell(2).setCellValue(device.getDeviceStatus().name());
            row.createCell(3).setCellValue(device.getBatteryLevel() != null ? device.getBatteryLevel() : 0);
            row.createCell(4).setCellValue(
                    device.getLastSyncAt() != null ? device.getLastSyncAt().format(DT_FORMAT) : "-");
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 배경색: #1A3A5C
        byte[] rgb = {0x1A, 0x3A, 0x5C};
        if (style instanceof org.apache.poi.xssf.usermodel.XSSFCellStyle xssfStyle) {
            xssfStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(rgb, null));
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private byte[] toByteArray(SXSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.dispose(); // 임시 파일 정리
        return out.toByteArray();
    }

    private String formatValue(BigDecimal numeric, String text) {
        if (numeric != null) {
            return numeric.stripTrailingZeros().toPlainString();
        }
        return text != null ? text : "";
    }

    private String formatDuration(Integer durationSec) {
        if (durationSec == null || durationSec == 0) {
            return "";
        }
        int hours = durationSec / 3600;
        int minutes = (durationSec % 3600) / 60;
        if (hours > 0 && minutes > 0) {
            return hours + "시간 " + minutes + "분";
        } else if (hours > 0) {
            return hours + "시간";
        } else {
            return minutes + "분";
        }
    }

    private String categoryToKorean(String category) {
        return switch (category) {
            case "VITAL_SIGN" -> "생체신호";
            case "ACTIVITY" -> "활동";
            case "SLEEP" -> "수면";
            case "AI_SCORE" -> "AI종합";
            default -> category;
        };
    }
}
