package com.pharmacy.orders.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtUserPrincipal {
    private Long userId;
    private String email;
    private String role;
}
