package com.wearable.monitor.api.auth;

import com.wearable.monitor.api.auth.dto.LoginRequestDto;
import com.wearable.monitor.api.auth.dto.TokenResponseDto;
import com.wearable.monitor.common.ApiResponse;
import com.wearable.monitor.config.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        TokenResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(
            @RequestHeader("Authorization") String bearerToken) {
        String refreshToken = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;
        TokenResponseDto response = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal String username) {
        authService.logout(username);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
