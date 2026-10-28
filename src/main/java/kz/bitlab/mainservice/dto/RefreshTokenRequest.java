package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to get a new access token with a refresh token")
public class RefreshTokenRequest {

    @NotBlank
    @ToString.Exclude
    @Schema(description = "Refresh token received from login or a previous refresh",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
