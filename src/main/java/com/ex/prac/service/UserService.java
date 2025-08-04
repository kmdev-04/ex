package com.ex.prac.service;

import com.ex.prac.dto.AuthResponseDto;
import com.ex.prac.dto.LoginRequestDto;
import com.ex.prac.dto.SignupRequestDto;
import com.ex.prac.dto.UserDto;
import com.ex.prac.entity.User;
import com.ex.prac.exception.EmailNotFoundException;
import com.ex.prac.redis.RedisService;
import com.ex.prac.repository.UserRepository;
import com.ex.prac.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // ← 추가
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public UserDto signup(SignupRequestDto dto) {
        log.info("회원가입 요청 - username: {}, email: {}", dto.getUsername(), dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("중복 이메일: {}", dto.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("중복 유저네임: {}", dto.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);

        UserDto result = userRepository.save(user).toDto();

        log.info("회원가입 성공 - userId: {}", user.getId());
        return result;
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        log.info("로그인 요청 - email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("이메일 없음: {}", dto.getEmail());
                    return new IllegalArgumentException("존재하지 않는 이메일입니다.");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("비밀번호 불일치 - email: {}", dto.getEmail());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String userId = user.getId().toString();
        String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 🔐 Redis에 RefreshToken 저장
        redisService.saveRefreshToken(userId, refreshToken);

        log.info("로그인 성공 - userId: {}", userId);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();
    }

    public AuthResponseDto refreshAccessToken(String refreshToken) {
        // 1. refreshToken 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. Claims에서 userId 추출
        String userId = jwtTokenProvider.getClaims(refreshToken).getSubject();

        // 3. Redis에 저장된 토큰과 일치하는지 확인
        String savedRefresh = redisService.getRefreshToken(userId);
        if (!refreshToken.equals(savedRefresh)) {
            throw new IllegalArgumentException("이미 만료되었거나 일치하지 않는 리프레시 토큰입니다.");
        }

        // 4. 사용자 정보 조회
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 5. 새로운 accessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // refreshToken은 그대로 반환
                .username(user.getUsername())
                .build();
    }

    public void logout(String accessToken) {
        // accessToken에서 userId 추출
        String userId;
        try {
            Claims claims = jwtTokenProvider.getClaims(accessToken);
            userId = claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 access token입니다.");
        }

        // Redis에서 refreshToken 삭제
        redisService.deleteRefreshToken(userId);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("사용자를 찾을 수 없습니다."));
        return user.toDto();
    }
}
