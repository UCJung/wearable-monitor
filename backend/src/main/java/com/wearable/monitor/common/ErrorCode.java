package com.wearable.monitor.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 인증
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("REFRESH_TOKEN_NOT_FOUND", "Refresh Token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    STAFF_ONLY_LOGIN("STAFF_ONLY_LOGIN", "웹은 의료진 계정만 로그인할 수 있습니다.", HttpStatus.FORBIDDEN),
    PATIENT_ONLY_LOGIN("PATIENT_ONLY_LOGIN", "앱은 환자 계정만 로그인할 수 있습니다.", HttpStatus.FORBIDDEN),

    // 환자
    PATIENT_NOT_FOUND("PATIENT_NOT_FOUND", "환자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PATIENT_CODE_DUPLICATE("PATIENT_CODE_DUPLICATE", "이미 존재하는 환자 코드입니다.", HttpStatus.CONFLICT),

    // 장치
    DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "장치를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DEVICE_SERIAL_DUPLICATE("DEVICE_SERIAL_DUPLICATE", "이미 등록된 시리얼 번호입니다.", HttpStatus.CONFLICT),
    DEVICE_NOT_AVAILABLE("DEVICE_NOT_AVAILABLE", "할당 가능한 상태의 장치가 아닙니다.", HttpStatus.BAD_REQUEST),

    // 할당
    ASSIGNMENT_NOT_FOUND("ASSIGNMENT_NOT_FOUND", "할당 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ASSIGNMENT_ALREADY_EXISTS("ASSIGNMENT_ALREADY_EXISTS", "이미 장치가 할당된 환자입니다.", HttpStatus.CONFLICT),
    PATIENT_HAS_ACTIVE_DEVICE("PATIENT_HAS_ACTIVE_DEVICE", "활성 할당이 있는 환자는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    DEVICE_ALREADY_ASSIGNED("DEVICE_ALREADY_ASSIGNED", "이미 할당된 장치입니다.", HttpStatus.CONFLICT),
    DEVICE_IS_ASSIGNED_CANNOT_DELETE("DEVICE_IS_ASSIGNED_CANNOT_DELETE", "할당 중인 장치는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PATIENT_ALREADY_HAS_DEVICE("PATIENT_ALREADY_HAS_DEVICE", "이미 장치가 할당된 환자입니다.", HttpStatus.CONFLICT),

    // 수집 항목 정의
    ITEM_DEF_NOT_FOUND("ITEM_DEF_NOT_FOUND", "수집 항목 정의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 생체신호 수집
    BATCH_EMPTY("BATCH_EMPTY", "업로드 데이터가 비어있습니다.", HttpStatus.BAD_REQUEST),
    BATCH_SIZE_EXCEEDED("BATCH_SIZE_EXCEEDED", "배치 업로드는 최대 500건까지 가능합니다.", HttpStatus.BAD_REQUEST),
    DATE_RANGE_EXCEEDED("DATE_RANGE_EXCEEDED", "조회 기간이 허용 범위를 초과합니다.", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_NOT_ACTIVE("ASSIGNMENT_NOT_ACTIVE", "활성 할당 정보가 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
