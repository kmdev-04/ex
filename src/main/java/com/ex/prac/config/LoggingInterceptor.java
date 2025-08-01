package com.ex.prac.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 여긴 실제 로그인한 사용자 정보를 넣어야 해 (예: 세션, JWT, SecurityContext 등에서 꺼냄)
        // 지금은 예시로 하드코딩:
        MDC.put("userId", "123");
        MDC.put("email", "test@example.com");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear(); // 끝나면 꼭 비워줘야 스레드 간 오염 방지됨
    }
}

