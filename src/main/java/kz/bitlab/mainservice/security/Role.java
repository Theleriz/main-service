package kz.bitlab.mainservice.security;

/**
 * Realm roles of the lms realm in Keycloak. The names are used as-is as Spring Security
 * GrantedAuthority, so hasRole("ADMIN") matches ROLE_ADMIN.
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_TEACHER,
    ROLE_USER;

    public static boolean isKnown(String name) {
        for (Role role : values()) {
            if (role.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
