package com.pharmacy.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of validating a JWT with the identity service")
public class TokenValidationResponse {

    @Schema(description = "Whether the token is currently valid", example = "true")
    private boolean valid;
}
