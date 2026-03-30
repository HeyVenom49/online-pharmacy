package com.pharmacy.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.blacklist.prefix:jwt:blacklist:}")
    private String blacklistPrefix;

    @Value("${jwt.blacklist.ttl:86400000}")
    private long blacklistTtl;

    public void blacklistToken(String token, long expirationTime) {
        String key = blacklistPrefix + token;
        long ttl = calculateTtl(expirationTime);
        
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            log.info("Token blacklisted successfully, TTL: {}ms", ttl);
        } else {
            log.warn("Token has already expired, not blacklisting");
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = blacklistPrefix + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void removeFromBlacklist(String token) {
        String key = blacklistPrefix + token;
        redisTemplate.delete(key);
        log.info("Token removed from blacklist");
    }

    private long calculateTtl(long expirationTime) {
        long currentTime = System.currentTimeMillis();
        return expirationTime - currentTime;
    }
}
