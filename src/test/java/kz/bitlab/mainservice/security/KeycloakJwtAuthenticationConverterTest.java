package kz.bitlab.mainservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    @Test
    void convert_mapsTokenToAuthUserWithRealmRoles() {
        Jwt jwt = jwt(Map.of("roles", List.of("ROLE_TEACHER", "offline_access", "default-roles-lms")));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TEACHER");
        assertThat(authentication.getPrincipal()).isInstanceOfSatisfying(AuthUser.class, user -> {
            assertThat(user.getId()).isEqualTo("user-id");
            assertThat(user.getUsername()).isEqualTo("teacher");
            assertThat(user.getEmail()).isEqualTo("teacher@bitlab.kz");
            assertThat(user.isAdmin()).isFalse();
        });
        assertThat(authentication.getCredentials()).isSameAs(jwt);
    }

    @Test
    void convert_admin_isAdmin() {
        AbstractAuthenticationToken authentication = converter.convert(jwt(Map.of("roles", List.of("ROLE_ADMIN"))));

        assertThat(((AuthUser) authentication.getPrincipal()).isAdmin()).isTrue();
    }

    @Test
    void convert_withoutRealmAccess_hasNoAuthorities() {
        AbstractAuthenticationToken authentication = converter.convert(jwt(null));

        assertThat(authentication.getAuthorities()).isEmpty();
    }

    private Jwt jwt(Map<String, Object> realmAccess) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-id")
                .claim("preferred_username", "teacher")
                .claim("email", "teacher@bitlab.kz")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        if (realmAccess != null) {
            builder.claim("realm_access", realmAccess);
        }
        return builder.build();
    }
}
