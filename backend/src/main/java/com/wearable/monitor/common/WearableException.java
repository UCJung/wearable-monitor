package com.wearable.monitor.common;

import lombok.Getter;

@Getter
public class WearableException extends RuntimeException {

    private final ErrorCode errorCode;

    public WearableException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public WearableException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }
}
