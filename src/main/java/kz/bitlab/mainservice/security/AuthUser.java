package kz.bitlab.mainservice.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Spring Security {@link User} built from a Keycloak access token.
 * Keeps the Keycloak user id (the sub claim) and email in addition to username and authorities.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class AuthUser extends User {

    private final String id;
    private final String email;

    public AuthUser(String id, String username, String email, Collection<? extends GrantedAuthority> authorities) {
        // There is no password: the user is already authenticated by Keycloak
        super(username, "", authorities);
        this.id = id;
        this.email = email;
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(authority -> Role.ROLE_ADMIN.name().equals(authority.getAuthority()));
    }
}
