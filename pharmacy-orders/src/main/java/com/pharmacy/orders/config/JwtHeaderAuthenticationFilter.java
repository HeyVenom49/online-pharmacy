package com.pharmacy.orders.config;

import com.pharmacy.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@Slf4j
public class JwtHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String AUTHORIZATION = "Authorization";

    private final JwtUtil jwtUtil;

    public JwtHeaderAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<com.pharmacy.orders.security.JwtUserPrincipal> fromGatewayHeaders = principalFromGatewayHeaders(request);
            Optional<com.pharmacy.orders.security.JwtUserPrincipal> fromBearerToken = fromGatewayHeaders.isPresent()
                    ? Optional.empty()
                    : principalFromBearerToken(request);

            fromGatewayHeaders.or(() -> fromBearerToken).ifPresent(principal -> {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + principal.getRole());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(authority)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private Optional<com.pharmacy.orders.security.JwtUserPrincipal> principalFromGatewayHeaders(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String email = request.getHeader(HEADER_USER_EMAIL);
        String role = request.getHeader(HEADER_USER_ROLE);

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(email)) {
            return Optional.empty();
        }

        String resolvedRole = StringUtils.hasText(role) ? role : "CUSTOMER";
        try {
            com.pharmacy.orders.security.JwtUserPrincipal principal =
                    new com.pharmacy.orders.security.JwtUserPrincipal(Long.parseLong(userId), email, resolvedRole);
            log.debug("Authenticated user from gateway headers: {} ({})", email, userId);
            return Optional.of(principal);
        } catch (NumberFormatException ex) {
            log.warn("Invalid X-User-Id header: {}", userId);
            return Optional.empty();
        }
    }

    private Optional<com.pharmacy.orders.security.JwtUserPrincipal> principalFromBearerToken(HttpServletRequest request) {
        String auth = request.getHeader(AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = auth.substring(7).trim();
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            return Optional.empty();
        }

        Number userIdNumber = jwtUtil.extractClaim(token, claims -> claims.get("userId", Number.class));
        Long userId = userIdNumber != null ? userIdNumber.longValue() : null;
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        if (userId == null || !StringUtils.hasText(email)) {
            return Optional.empty();
        }

        String resolvedRole = StringUtils.hasText(role) ? role : "CUSTOMER";
        com.pharmacy.orders.security.JwtUserPrincipal principal =
                new com.pharmacy.orders.security.JwtUserPrincipal(userId, email, resolvedRole);
        log.debug("Authenticated user from bearer token: {} ({})", email, userId);
        return Optional.of(principal);
    }
}
