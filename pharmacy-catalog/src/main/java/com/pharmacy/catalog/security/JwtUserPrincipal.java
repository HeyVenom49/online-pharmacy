package com.pharmacy.catalog.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtUserPrincipal {
    private Long userId;
    private String email;
    private String role;
}
