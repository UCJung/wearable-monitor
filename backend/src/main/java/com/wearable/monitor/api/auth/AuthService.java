package com.wearable.monitor.api.auth;

import com.wearable.monitor.api.auth.dto.LoginRequestDto;
import com.wearable.monitor.api.auth.dto.TokenResponseDto;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.config.JwtProvider;
import com.wearable.monitor.domain.user.User;
import com.wearable.monitor.domain.user.UserRepository;
import com.wearable.monitor.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private static final long REFRESH_TTL_DAYS = 7L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new WearableException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new WearableException(ErrorCode.INVALID_CREDENTIALS);
        }

        // platform 기반 역할 검증
        String platform = dto.getPlatform();
        if ("WEB".equalsIgnoreCase(platform) && user.getRole() != UserRole.STAFF) {
            throw new WearableException(ErrorCode.STAFF_ONLY_LOGIN);
        }
        if ("ANDROID".equalsIgnoreCase(platform) && user.getRole() != UserRole.PATIENT) {
            throw new WearableException(ErrorCode.PATIENT_ONLY_LOGIN);
        }

        String role = user.getRole().name();
        String accessToken = jwtProvider.createAccessToken(user.getUsername(), role);
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername(), role);

        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + user.getUsername(),
                refreshToken,
                REFRESH_TTL_DAYS,
                TimeUnit.DAYS
        );

        log.info("[AuthService] 로그인 성공: username={}, role={}, platform={}",
                user.getUsername(), role, platform);
        return new TokenResponseDto(accessToken, refreshToken, role, user.getUsername());
    }

    public TokenResponseDto refresh(String refreshToken) {
        jwtProvider.validate(refreshToken);
        String username = jwtProvider.getSubject(refreshToken);

        String stored = redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + username);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new WearableException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new WearableException(ErrorCode.INVALID_CREDENTIALS));

        String role = user.getRole().name();
        String newAccessToken = jwtProvider.createAccessToken(username, role);
        log.info("[AuthService] 토큰 재발급: username={}", username);

        return new TokenResponseDto(newAccessToken, refreshToken, role, username);
    }

    public void logout(String username) {
        redisTemplate.delete(REFRESH_KEY_PREFIX + username);
        log.info("[AuthService] 로그아웃: username={}", username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
