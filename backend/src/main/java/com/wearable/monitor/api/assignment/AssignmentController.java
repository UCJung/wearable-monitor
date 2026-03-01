package com.wearable.monitor.api.assignment;

import com.wearable.monitor.api.assignment.dto.AssignDeviceRequest;
import com.wearable.monitor.api.assignment.dto.AssignmentListResponse;
import com.wearable.monitor.api.assignment.dto.ReturnDeviceRequest;
import com.wearable.monitor.common.ApiResponse;
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
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentListResponse>> assignDevice(
            @Valid @RequestBody AssignDeviceRequest request) {
        AssignmentListResponse response = assignmentService.assignDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<AssignmentListResponse>> returnDevice(
            @PathVariable Long id,
            @Valid @RequestBody ReturnDeviceRequest request) {
        AssignmentListResponse response = assignmentService.returnDevice(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AssignmentListResponse>>> getAssignments(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AssignmentListResponse> response = assignmentService.getAssignments(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentListResponse>> getAssignment(@PathVariable Long id) {
        AssignmentListResponse response = assignmentService.getAssignment(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
