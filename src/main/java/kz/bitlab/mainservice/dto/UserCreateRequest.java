package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kz.bitlab.mainservice.security.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to register a user (admin only)")
public class UserCreateRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Username", example = "john", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Email
    @Schema(description = "Email", example = "john@bitlab.kz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Last name", example = "Smith", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank
    @Size(min = 6, max = 100)
    @ToString.Exclude
    @Schema(description = "Password", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @ArraySchema(arraySchema = @Schema(description = "Roles of the user, ROLE_USER if empty",
            example = "[\"ROLE_TEACHER\"]"))
    private Set<Role> roles;
}
