package com.wearable.monitor.api.patient;

import com.wearable.monitor.api.patient.dto.*;
import com.wearable.monitor.common.ApiResponse;
import com.wearable.monitor.domain.patient.PatientStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<ApiResponse<PatientDetailResponse>> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {
        PatientDetailResponse response = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PatientListResponse>>> getPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String patientCode,
            @RequestParam(required = false) PatientStatus status,
            @RequestParam(required = false) Boolean hasDevice,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        PatientSearchCondition condition = new PatientSearchCondition();
        condition.setName(name);
        condition.setPatientCode(patientCode);
        condition.setStatus(status);
        condition.setHasDevice(hasDevice);

        Page<PatientListResponse> response = patientService.getPatients(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDetailResponse>> getPatientDetail(@PathVariable Long id) {
        PatientDetailResponse response = patientService.getPatientDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDetailResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request) {
        PatientDetailResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
