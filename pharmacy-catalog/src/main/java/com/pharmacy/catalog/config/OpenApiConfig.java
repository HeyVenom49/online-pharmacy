package com.pharmacy.catalog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${services.identity.url:http://localhost:8081}")
    private String identityUrl;

    @Value("${services.catalog.url:http://localhost:8082}")
    private String catalogUrl;

    @Value("${services.orders.url:http://localhost:8083}")
    private String ordersUrl;

    @Value("${services.admin.url:http://localhost:8084}")
    private String adminUrl;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI catalogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pharmacy API - Catalog Service")
                        .version("1.0.0")
                        .description("""
                                ## Catalog Service - Medicines, Prescriptions & Inventory
                                
                                ### Quick Start
                                1. **Browse Medicines**: GET `/api/catalog/medicines` - Public endpoint
                                2. **Search**: GET `/api/catalog/medicines/search` - Search with filters
                                3. **Prescriptions**: POST `/api/catalog/prescriptions/upload` - Requires auth
                                
                                ### Public vs Protected
                                - Public: Browse, search, view medicines
                                - Protected: Upload prescriptions, manage favorites
                                
                                ### Switch server dropdown to explore other services
                                """)
                        .contact(new Contact()
                                .name("Pharmacy Team")
                                .email("support@pharmacy.com")))
                .servers(List.of(
                        new Server().url(identityUrl).description("Identity Service"),
                        new Server().url(catalogUrl).description("Catalog Service"),
                        new Server().url(ordersUrl).description("Orders Service"),
                        new Server().url(adminUrl).description("Admin Service")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token from /api/auth/login")))
                .security(List.of(new SecurityRequirement().addList(SECURITY_SCHEME_NAME)));
    }
}
