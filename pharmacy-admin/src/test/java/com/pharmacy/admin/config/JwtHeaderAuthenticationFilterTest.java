package com.pharmacy.admin.config;

import com.pharmacy.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class JwtHeaderAuthenticationFilterTest {

    private final JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
    private final JwtHeaderAuthenticationFilter filter = new JwtHeaderAuthenticationFilter(jwtUtil);

    @BeforeEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsAuthenticationWhenHeadersPresent() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-User-Id", "42");
        req.addHeader("X-User-Email", "a@test.com");
        req.addHeader("X-User-Role", "ADMIN");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("a@test.com",
                ((AdminUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
    }

    @Test
    void noAuthWhenHeadersMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
