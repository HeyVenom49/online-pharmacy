package com.pharmacy.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtBlacklistService, "blacklistPrefix", "jwt:blacklist:");
        ReflectionTestUtils.setField(jwtBlacklistService, "blacklistTtl", 86400000L);
    }

    private void stubValueOps() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void blacklistToken_shouldAddTokenToRedis() {
        stubValueOps();
        String token = "test.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000;

        jwtBlacklistService.blacklistToken(token, expirationTime);

        verify(valueOperations).set(
                eq("jwt:blacklist:test.jwt.token"),
                eq("blacklisted"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void blacklistToken_shouldNotAddExpiredToken() {
        String token = "expired.jwt.token";
        long expirationTime = System.currentTimeMillis() - 1000;

        jwtBlacklistService.blacklistToken(token, expirationTime);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void isTokenBlacklisted_shouldReturnTrueWhenTokenExists() {
        String token = "blacklisted.jwt.token";
        when(redisTemplate.hasKey("jwt:blacklist:blacklisted.jwt.token")).thenReturn(true);

        boolean result = jwtBlacklistService.isTokenBlacklisted(token);

        assertTrue(result);
        verify(redisTemplate).hasKey("jwt:blacklist:blacklisted.jwt.token");
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseWhenTokenDoesNotExist() {
        String token = "valid.jwt.token";
        when(redisTemplate.hasKey("jwt:blacklist:valid.jwt.token")).thenReturn(false);

        boolean result = jwtBlacklistService.isTokenBlacklisted(token);

        assertFalse(result);
        verify(redisTemplate).hasKey("jwt:blacklist:valid.jwt.token");
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseOnNullKey() {
        String token = "some.jwt.token";
        when(redisTemplate.hasKey("jwt:blacklist:some.jwt.token")).thenReturn(null);

        boolean result = jwtBlacklistService.isTokenBlacklisted(token);

        assertFalse(result);
    }

    @Test
    void removeFromBlacklist_shouldDeleteTokenFromRedis() {
        String token = "token.to.remove";

        jwtBlacklistService.removeFromBlacklist(token);

        verify(redisTemplate).delete("jwt:blacklist:token.to.remove");
    }

    @Test
    void blacklistToken_withBearerPrefix_shouldExtractToken() {
        stubValueOps();
        String bearerToken = "Bearer test.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000;

        jwtBlacklistService.blacklistToken(bearerToken, expirationTime);

        verify(valueOperations).set(
                eq("jwt:blacklist:Bearer test.jwt.token"),
                eq("blacklisted"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }
}
