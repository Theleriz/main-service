package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT access token and refresh token issued by Keycloak")
public class TokenResponse {

    @ToString.Exclude
    @Schema(description = "JWT access token, send it as 'Authorization: Bearer <token>'")
    private String accessToken;

    @ToString.Exclude
    @Schema(description = "Refresh token for /api/v1/auth/refresh")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Access token lifetime in seconds", example = "300")
    private Long expiresIn;

    @Schema(description = "Refresh token lifetime in seconds", example = "604800")
    private Long refreshExpiresIn;
}
