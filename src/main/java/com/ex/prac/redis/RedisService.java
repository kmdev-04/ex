package com.ex.prac.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    @Autowired
    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, Duration.ofDays(7));
        System.out.println("✅ Redis 저장됨: refresh:" + userId);

        String val = redisTemplate.opsForValue().get("refresh:" + userId);
        System.out.println("💾 Redis에서 읽은 값: " + val);

        Long ttl = redisTemplate.getExpire("refresh:" + userId, TimeUnit.SECONDS);
        System.out.println("TTL: " + ttl);

        System.out.println("📡 Redis ping: " + redisTemplate.getConnectionFactory().getConnection().ping());

        System.out.println("현재 Redis에 저장된 키: " + redisTemplate.keys("*"));
        System.out.println("현재 사용 중인 Redis DB Index: " +
                ((LettuceConnectionFactory) redisTemplate.getConnectionFactory()).getDatabase());

        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory instanceof LettuceConnectionFactory lettuce) {
            System.out.println("Host: " + lettuce.getHostName());
            System.out.println("Port: " + lettuce.getPort());
            System.out.println("Database: " + lettuce.getDatabase());
        }
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    public void addAccessTokenToBlacklist(String token, long expirationMillis) {
        redisTemplate.opsForValue().set("blacklist:" + token, "logout", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}