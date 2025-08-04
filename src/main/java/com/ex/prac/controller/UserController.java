package com.ex.prac.controller;

import com.ex.prac.dto.*;
import com.ex.prac.redis.RedisService;
import com.ex.prac.security.CustomUserDetails;
import com.ex.prac.security.JwtTokenProvider;
import com.ex.prac.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto dto) {
        UserDto savedUser = userService.signup(dto);
        System.out.println("🔔 signup 컨트롤러 호출됨: " + dto.getEmail());
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto,
                                   HttpServletResponse response) {

        AuthResponseDto auth = userService.login(dto); // access + refresh 발급됨

        // ✅ 쿠키로 내려보내기
        setTokenCookie(response, "accessToken", auth.getAccessToken(), 60 * 15); // 15분
        setTokenCookie(response, "refreshToken", auth.getRefreshToken(), 60 * 60 * 24 * 7); // 7일

        // ✅ 응답 본문에는 username만
        return ResponseEntity.ok(Map.of("username", auth.getUsername()));
    }

    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody TokenRefreshRequest request) {
        AuthResponseDto response = userService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long expiration = jwtTokenProvider.getTokenExpiration(token); // 남은 유효 시간(ms)
            redisService.addAccessTokenToBlacklist(token, expiration);
            // refreshToken도 삭제
            String userId = jwtTokenProvider.getClaims(token).getSubject();
            redisService.deleteRefreshToken(userId);
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminPage() {
        return "관리자 전용 페이지입니다.";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user")
    public String userOrAdmin() {
        return "일반 사용자 및 관리자 접근 가능";
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserInfo(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserDto userDto = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userDto);
    }
}