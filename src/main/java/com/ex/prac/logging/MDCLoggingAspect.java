package com.ex.prac.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MDCLoggingAspect {

    @Before("execution(* com.ex.prac.controller..*(..)) || execution(* com.ex.prac.service..*(..))")
    public void setUserContextInMDC() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                MDC.put("userId", userDetails.getUsername()); // 일반적으로 username이 userId 대용
                MDC.put("email", userDetails.getUsername());  // 필요시 따로 email 필드 분리 가능
            } else {
                MDC.put("userId", "anonymous");
                MDC.put("email", "anonymous");
            }
        } catch (Exception e) {
            MDC.put("userId", "unknown");
            MDC.put("email", "unknown");
            log.warn("MDC 설정 실패: {}", e.getMessage());
        }
    }
}
