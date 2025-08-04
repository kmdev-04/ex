package com.ex.prac.security;

import com.ex.prac.redis.RedisService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String accessToken = resolveAccessToken(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                if (redisService.isAccessTokenBlacklisted(accessToken)) {
                    logger.warn("블랙리스트 토큰 - access denied");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Claims claims = jwtTokenProvider.getClaims(accessToken);
                String email = claims.get("email", String.class);
                String userId = claims.getSubject();
                authenticate(email, userId, request);

            } else if (accessToken != null) {
                // accessToken 유효하지 않은 경우 (→ RefreshToken으로 재발급)
                String refreshToken = resolveRefreshToken(request);
                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    Claims claims = jwtTokenProvider.getClaims(refreshToken);
                    String userId = claims.getSubject();
                    String savedRefresh = redisService.getRefreshToken(userId);

                    if (refreshToken.equals(savedRefresh)) {
                        String email = claims.get("email", String.class);
                        authenticate(email, userId, request);
                        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email);
                        response.setHeader("Authorization", "Bearer " + newAccessToken);
                    }
                }
            }

            // ✅ 모든 경우에 필터 통과시킴
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void authenticate(String email, String userId, HttpServletRequest request) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MDC.put("email", email);
        MDC.put("userId", userId);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("Refresh-Token");
    }
}


