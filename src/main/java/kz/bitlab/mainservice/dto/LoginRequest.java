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
@Schema(description = "Login with username and password")
public class LoginRequest {

    @NotBlank
    @Schema(description = "Username or email", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @ToString.Exclude
    @Schema(description = "Password", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
