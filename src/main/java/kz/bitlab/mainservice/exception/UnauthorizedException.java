package kz.bitlab.mainservice.exception;

/**
 * Thrown when Keycloak rejects the credentials or the refresh token. Handled as 401 UNAUTHORIZED.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
