package com.wearable.monitor.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {

    private final String accessToken;
    private final String refreshToken;
    private final String role;
    private final String username;
}
