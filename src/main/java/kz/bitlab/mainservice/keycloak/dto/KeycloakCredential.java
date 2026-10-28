package kz.bitlab.mainservice.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Keycloak's CredentialRepresentation for a password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakCredential {

    private String type;

    @ToString.Exclude
    private String value;

    private Boolean temporary;

    public static KeycloakCredential password(String password) {
        return new KeycloakCredential("password", password, false);
    }
}
