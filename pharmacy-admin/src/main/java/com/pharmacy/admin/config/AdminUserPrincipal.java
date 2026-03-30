package com.pharmacy.admin.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserPrincipal {
    private Long userId;
    private String email;
    private String role;
}
