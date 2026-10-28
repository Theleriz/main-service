package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import kz.bitlab.mainservice.security.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

/**
 * All fields are optional: only the fields that are present are changed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user data and password. Fields that are not sent stay unchanged")
public class UserUpdateRequest {

    @Email
    @Schema(description = "New email", example = "john.smith@bitlab.kz")
    private String email;

    @Size(min = 1, max = 255)
    @Schema(description = "New first name", example = "John")
    private String firstName;

    @Size(min = 1, max = 255)
    @Schema(description = "New last name", example = "Smith")
    private String lastName;

    @Size(min = 6, max = 100)
    @ToString.Exclude
    @Schema(description = "New password", example = "newSecret123")
    private String password;

    @ArraySchema(arraySchema = @Schema(description = "New set of roles. Only an admin can change roles",
            example = "[\"ROLE_TEACHER\"]"))
    private Set<Role> roles;
}
