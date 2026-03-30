package com.pharmacy.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=12345678901234567890123456789012",
        "jwt.expiration=3600000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken(42L, "a@b.com", "USER");
        assertEquals("a@b.com", jwtUtil.extractEmail(token));
        assertEquals(42L, jwtUtil.extractUserId(token));
        assertEquals("USER", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.validateToken(token, "a@b.com"));
        assertFalse(jwtUtil.validateToken(token, "other@b.com"));
        assertTrue(jwtUtil.getExpirationTime(token) > 0);
    }

    @Test
    void validateToken_invalidReturnsFalse() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
    }

    @Test
    void getExpirationTime_invalidReturnsZero() {
        assertEquals(0, jwtUtil.getExpirationTime("bad"));
    }
}
