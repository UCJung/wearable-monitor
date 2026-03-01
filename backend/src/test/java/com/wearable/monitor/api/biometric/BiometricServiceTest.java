package com.wearable.monitor.api.biometric;

import com.wearable.monitor.api.biometric.dto.BatchUploadRequest;
import com.wearable.monitor.api.biometric.dto.BatchUploadResponse;
import com.wearable.monitor.api.biometric.dto.BiometricHistoryResponse;
import com.wearable.monitor.api.biometric.dto.BiometricUploadItem;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.PatientBiometricHistory;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinition;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinitionRepository;
import com.wearable.monitor.domain.itemdef.CollectionMode;
import com.wearable.monitor.domain.itemdef.ItemCategory;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiometricServiceTest {

    @Mock
    private PatientBiometricHistoryRepository biometricRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;
    @Mock
    private CollectionItemDefinitionRepository itemDefRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BiometricService biometricService;

    private Device device;
    private PatientDeviceAssignment assignment;
    private CollectionItemDefinition hrItemDef;

    @BeforeEach
    void setUp() {
        device = spy(new Device("SN-WATCH-001", "Galaxy Watch 7"));
        lenient().when(device.getId()).thenReturn(1L);

        Patient patient = spy(new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null));
        lenient().when(patient.getId()).thenReturn(1L);

        assignment = spy(new PatientDeviceAssignment(patient, device, LocalDate.of(2026, 1, 1)));
        lenient().when(assignment.getId()).thenReturn(1L);

        hrItemDef = spy(new CollectionItemDefinition("HR", "심박수", ItemCategory.VITAL_SIGN,
                CollectionMode.CONTINUOUS, "HeartRate", "bpm", "1분", 1));
        lenient().when(hrItemDef.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("batchUpload — 정상 업로드 시 saved 카운트 반환")
    void batchUpload_success() {
        // given
        List<BiometricUploadItem> items = List.of(
                new BiometricUploadItem("HR", LocalDateTime.of(2026, 2, 1, 10, 0), BigDecimal.valueOf(72), null, null),
                new BiometricUploadItem("HR", LocalDateTime.of(2026, 2, 1, 10, 1), BigDecimal.valueOf(75), null, null)
        );
        BatchUploadRequest request = new BatchUploadRequest(items, "SN-WATCH-001");

        given(deviceRepository.findBySerialNumber("SN-WATCH-001")).willReturn(Optional.of(device));
        given(assignmentRepository.findByDeviceIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(itemDefRepository.findByIsActiveTrueOrderByDisplayOrder()).willReturn(List.of(hrItemDef));
        given(biometricRepository.existsByAssignmentIdAndItemDef_IdAndMeasuredAt(
                anyLong(), anyLong(), any(LocalDateTime.class))).willReturn(false);
        given(biometricRepository.save(any(PatientBiometricHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        BatchUploadResponse response = biometricService.batchUpload(request);

        // then
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getSaved()).isEqualTo(2);
        assertThat(response.getSkipped()).isEqualTo(0);
        assertThat(response.getFailed()).isEqualTo(0);
        verify(biometricRepository, times(2)).save(any(PatientBiometricHistory.class));
    }

    @Test
    @DisplayName("batchUpload — 중복 데이터 스킵 처리")
    void batchUpload_duplicateSkip() {
        // given
        List<BiometricUploadItem> items = List.of(
                new BiometricUploadItem("HR", LocalDateTime.of(2026, 2, 1, 10, 0), BigDecimal.valueOf(72), null, null),
                new BiometricUploadItem("HR", LocalDateTime.of(2026, 2, 1, 10, 1), BigDecimal.valueOf(75), null, null)
        );
        BatchUploadRequest request = new BatchUploadRequest(items, "SN-WATCH-001");

        given(deviceRepository.findBySerialNumber("SN-WATCH-001")).willReturn(Optional.of(device));
        given(assignmentRepository.findByDeviceIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(itemDefRepository.findByIsActiveTrueOrderByDisplayOrder()).willReturn(List.of(hrItemDef));

        // 첫 번째는 존재하지 않음, 두 번째는 중복 존재
        given(biometricRepository.existsByAssignmentIdAndItemDef_IdAndMeasuredAt(
                anyLong(), anyLong(), any(LocalDateTime.class)))
                .willReturn(false)
                .willReturn(true);
        given(biometricRepository.save(any(PatientBiometricHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        BatchUploadResponse response = biometricService.batchUpload(request);

        // then
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getSkipped()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(0);
    }

    @Test
    @DisplayName("batchUpload — 빈 목록 시 BATCH_EMPTY 예외")
    void batchUpload_emptyList() {
        BatchUploadRequest request = new BatchUploadRequest(List.of(), "SN-WATCH-001");

        assertThatThrownBy(() -> biometricService.batchUpload(request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_EMPTY));
    }

    @Test
    @DisplayName("batchUpload — 500건 초과 시 BATCH_SIZE_EXCEEDED 예외")
    void batchUpload_exceedsMaxSize() {
        List<BiometricUploadItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 501; i++) {
            items.add(new BiometricUploadItem("HR", LocalDateTime.now(), BigDecimal.valueOf(72), null, null));
        }
        BatchUploadRequest request = new BatchUploadRequest(items, "SN-WATCH-001");

        assertThatThrownBy(() -> biometricService.batchUpload(request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_SIZE_EXCEEDED));
    }

    @Test
    @DisplayName("batchUpload — device last_sync_at 갱신 확인")
    void batchUpload_updatesLastSyncAt() {
        // given
        List<BiometricUploadItem> items = List.of(
                new BiometricUploadItem("HR", LocalDateTime.of(2026, 2, 1, 10, 0), BigDecimal.valueOf(72), null, null)
        );
        BatchUploadRequest request = new BatchUploadRequest(items, "SN-WATCH-001");

        given(deviceRepository.findBySerialNumber("SN-WATCH-001")).willReturn(Optional.of(device));
        given(assignmentRepository.findByDeviceIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(itemDefRepository.findByIsActiveTrueOrderByDisplayOrder()).willReturn(List.of(hrItemDef));
        given(biometricRepository.save(any(PatientBiometricHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        biometricService.batchUpload(request);

        // then
        assertThat(device.getLastSyncAt()).isNotNull();
    }

    @Test
    @DisplayName("getHistory — 90일 초과 범위 시 DATE_RANGE_EXCEEDED 예외")
    void getHistory_exceedsMaxDays() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 5, 1); // > 90일

        assertThatThrownBy(() -> biometricService.getHistory(1L, null, start, end, PageRequest.of(0, 100)))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DATE_RANGE_EXCEEDED));
    }

    @Test
    @DisplayName("getHistory — 정상 조회 시 hasMore 플래그 확인")
    void getHistory_returnsRecords() {
        // given
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);
        Pageable pageable = PageRequest.of(0, 100);

        given(biometricRepository.findByCondition(eq(1L), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        BiometricHistoryResponse response = biometricService.getHistory(1L, null, start, end, pageable);

        // then
        assertThat(response.getRecords()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
        assertThat(response.isHasMore()).isFalse();
    }
}
