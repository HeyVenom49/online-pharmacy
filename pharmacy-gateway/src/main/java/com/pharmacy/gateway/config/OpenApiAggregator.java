package com.pharmacy.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Configuration
@Slf4j
@EnableScheduling
public class OpenApiAggregator {

    @Value("${services.identity.url:http://localhost:8081}")
    private String identityUrl;

    @Value("${services.catalog.url:http://localhost:8082}")
    private String catalogUrl;

    @Value("${services.orders.url:http://localhost:8083}")
    private String ordersUrl;

    @Value("${services.admin.url:http://localhost:8084}")
    private String adminUrl;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private volatile OpenAPI cachedOpenAPI;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        refreshSpecs();
    }

    @Scheduled(fixedRate = 60000)
    public void refreshSpecs() {
        log.debug("Refreshing OpenAPI specs...");
        cachedOpenAPI = buildOpenAPI();
    }

    @Bean
    @Primary
    public OpenAPI gatewayOpenAPI() {
        return cachedOpenAPI != null ? cachedOpenAPI : buildOpenAPI();
    }

    private OpenAPI buildOpenAPI() {
        OpenAPI combined = new OpenAPI();

        combined.info(new Info()
                .title("Pharmacy API - Unified Documentation")
                .version("1.0.0")
                .description("""
                        ## Pharmacy API Documentation
                        
                        ### Services
                        | Service | Port | Description |
                        |---------|------|-------------|
                        | Identity | 8081 | Auth, Users, Addresses |
                        | Catalog | 8082 | Medicines, Prescriptions, Inventory |
                        | Orders | 8083 | Cart, Orders, Payments |
                        | Admin | 8084 | Dashboard, KPIs |
                        
                        ### How to Authenticate
                        1. Use `POST /api/auth/login` with email/password
                        2. Copy the `token` from response
                        3. Click **Authorize** button above
                        4. Enter: `Bearer <your-token>`
                        5. Click **Authorize** then **Close**
                        
                        ### Test Users
                        | Email | Password | Role |
                        |-------|----------|------|
                        | john.doe@example.com | admin123 | CUSTOMER |
                        | admin@pharmacy.com | admin123 | ADMIN |
                        """)
                .contact(new Contact()
                        .name("Pharmacy Team")
                        .email("support@pharmacy.com")));

        combined.servers(List.of(
                new Server().url("http://localhost:8080").description("API Gateway (Recommended)"),
                new Server().url("http://localhost:8081").description("Identity Service"),
                new Server().url("http://localhost:8082").description("Catalog Service"),
                new Server().url("http://localhost:8083").description("Orders Service"),
                new Server().url("http://localhost:8084").description("Admin Service")
        ));

        combined.components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token. Get one from /api/auth/login endpoint.")));

        combined.security(List.of(new SecurityRequirement().addList(SECURITY_SCHEME_NAME)));

        Map<String, String> services = new LinkedHashMap<>();
        services.put("Identity", identityUrl + "/v3/api-docs");
        services.put("Catalog", catalogUrl + "/v3/api-docs");
        services.put("Orders", ordersUrl + "/v3/api-docs");
        services.put("Admin", adminUrl + "/v3/api-docs");

        int totalPaths = 0;
        for (Map.Entry<String, String> entry : services.entrySet()) {
            try {
                String serviceName = Objects.requireNonNull(entry.getKey(), "serviceName");
                String url = Objects.requireNonNull(entry.getValue(), "url");

                log.debug("Fetching OpenAPI spec from {} at {}", serviceName, url);
                String json = restTemplate.getForObject(url, String.class);
                if (json == null || json.isBlank()) {
                    log.warn("Empty OpenAPI response from {} at {}", serviceName, url);
                    continue;
                }
                JsonNode root = objectMapper.readTree(json);

                JsonNode paths = root.get("paths");
                if (paths != null && paths.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = paths.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String path = field.getKey();
                        JsonNode pathItem = field.getValue();

                        if (combined.getPaths() == null) {
                            combined.paths(new io.swagger.v3.oas.models.Paths());
                        }
                        if (!combined.getPaths().containsKey(path)) {
                            try {
                                PathItem converted = convertPathItem(pathItem);
                                combined.path(path, converted);
                                totalPaths++;
                            } catch (Exception e) {
                                log.debug("Failed to convert path {}: {}", path, e.getMessage());
                            }
                        }
                    }
                    log.info("Aggregated {} endpoints from {}", paths.size(), serviceName);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch OpenAPI spec from {}: {}", entry.getValue(), e.getMessage());
            }
        }

        if (combined.getPaths() == null || combined.getPaths().isEmpty()) {
            log.warn("No paths aggregated from services. Using fallback.");
            combined.paths(new io.swagger.v3.oas.models.Paths());
        }

        log.info("Total aggregated OpenAPI endpoints: {}", totalPaths);
        return combined;
    }

    /**
     * Some service specs (e.g. Map-based response types) emit {@code additionalProperties} shapes that
     * Jackson cannot bind to {@link io.swagger.v3.oas.models.media.Schema}. Normalize and retry.
     */
    private PathItem convertPathItem(JsonNode pathItem) {
        JsonNode copy = pathItem.deepCopy();
        replaceEmptyObjectAdditionalProperties(copy);
        try {
            return objectMapper.convertValue(copy, PathItem.class);
        } catch (IllegalArgumentException ex) {
            JsonNode relaxed = pathItem.deepCopy();
            coerceObjectAdditionalPropertiesToTrue(relaxed);
            return objectMapper.convertValue(relaxed, PathItem.class);
        }
    }

    private static void replaceEmptyObjectAdditionalProperties(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            JsonNode ap = obj.get("additionalProperties");
            if (ap != null && ap.isObject() && ap.isEmpty()) {
                obj.set("additionalProperties", BooleanNode.TRUE);
            }
            obj.fields().forEachRemaining(e -> replaceEmptyObjectAdditionalProperties(e.getValue()));
        } else if (node.isArray()) {
            node.forEach(OpenApiAggregator::replaceEmptyObjectAdditionalProperties);
        }
    }

    private static void coerceObjectAdditionalPropertiesToTrue(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            JsonNode ap = obj.get("additionalProperties");
            if (ap != null && ap.isObject()) {
                obj.set("additionalProperties", BooleanNode.TRUE);
            }
            obj.fields().forEachRemaining(e -> coerceObjectAdditionalPropertiesToTrue(e.getValue()));
        } else if (node.isArray()) {
            node.forEach(OpenApiAggregator::coerceObjectAdditionalPropertiesToTrue);
        }
    }
}
