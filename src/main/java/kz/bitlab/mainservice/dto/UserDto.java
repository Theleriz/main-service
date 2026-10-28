package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.bitlab.mainservice.security.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User stored in Keycloak")
public class UserDto {

    @Schema(description = "Keycloak user ID", example = "6f1c3c2e-1b5a-4a8e-9a43-2f3b0d2c9a11")
    private String id;

    @Schema(description = "Username", example = "john")
    private String username;

    @Schema(description = "Email", example = "john@bitlab.kz")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;

    @Schema(description = "Whether the user can log in", example = "true")
    private Boolean enabled;

    @Schema(description = "Roles (permissions) of the user", example = "[\"ROLE_USER\"]")
    private Set<Role> roles;
}
