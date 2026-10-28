package kz.bitlab.mainservice.keycloak;

import kz.bitlab.mainservice.exception.ConflictException;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.exception.UnauthorizedException;
import kz.bitlab.mainservice.keycloak.dto.KeycloakCredential;
import kz.bitlab.mainservice.keycloak.dto.KeycloakRole;
import kz.bitlab.mainservice.keycloak.dto.KeycloakTokenResponse;
import kz.bitlab.mainservice.keycloak.dto.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * HTTP client for Keycloak: the token endpoint of the lms realm and the Admin REST API.
 * Admin calls are authorized with a client_credentials token of the main service's service account.
 */
@Slf4j
@Component
public class KeycloakClient {

    private static final String USER_NOT_FOUND = "User with id %s not found";
    private static final String USER_EXISTS = "User with the same username or email already exists";
    // Refresh the admin token a bit before it actually expires
    private static final long TOKEN_EXPIRY_MARGIN_SECONDS = 30;

    private final KeycloakProperties properties;
    private final RestClient restClient;

    private String adminToken;
    private Instant adminTokenExpiresAt = Instant.EPOCH;

    public KeycloakClient(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.url()).build();
    }

    public KeycloakTokenResponse passwordGrant(String username, String password) {
        MultiValueMap<String, String> form = clientForm("password");
        form.add("username", username);
        form.add("password", password);
        return requestToken(form, "Invalid username or password");
    }

    public KeycloakTokenResponse refreshTokenGrant(String refreshToken) {
        MultiValueMap<String, String> form = clientForm("refresh_token");
        form.add("refresh_token", refreshToken);
        return requestToken(form, "Invalid or expired refresh token");
    }

    public String createUser(KeycloakUser user) {
        URI location = restClient.post()
                .uri(adminPath("/users"))
                .headers(this::adminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (request, response) -> {
                    throw new ConflictException(USER_EXISTS);
                })
                .toBodilessEntity()
                .getHeaders()
                .getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak did not return the id of the created user");
        }
        // Location: .../admin/realms/lms/users/{id}
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public KeycloakUser getUser(String id) {
        return restClient.get()
                .uri(adminPath("/users/{id}"), id)
                .headers(this::adminAuth)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new NotFoundException(USER_NOT_FOUND.formatted(id));
                })
                .body(KeycloakUser.class);
    }

    public void updateUser(String id, KeycloakUser user) {
        restClient.put()
                .uri(adminPath("/users/{id}"), id)
                .headers(this::adminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new NotFoundException(USER_NOT_FOUND.formatted(id));
                })
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (request, response) -> {
                    throw new ConflictException(USER_EXISTS);
                })
                .toBodilessEntity();
    }

    public void resetPassword(String id, String password) {
        restClient.put()
                .uri(adminPath("/users/{id}/reset-password"), id)
                .headers(this::adminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(KeycloakCredential.password(password))
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new NotFoundException(USER_NOT_FOUND.formatted(id));
                })
                .toBodilessEntity();
    }

    public KeycloakRole getRealmRole(String name) {
        return restClient.get()
                .uri(adminPath("/roles/{name}"), name)
                .headers(this::adminAuth)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new IllegalStateException("Role " + name + " does not exist in Keycloak");
                })
                .body(KeycloakRole.class);
    }

    public List<KeycloakRole> getUserRealmRoles(String userId) {
        List<KeycloakRole> roles = restClient.get()
                .uri(adminPath("/users/{id}/role-mappings/realm"), userId)
                .headers(this::adminAuth)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new NotFoundException(USER_NOT_FOUND.formatted(userId));
                })
                .body(new ParameterizedTypeReference<>() {
                });
        return roles != null ? roles : List.of();
    }

    public void addUserRealmRoles(String userId, List<KeycloakRole> roles) {
        changeUserRealmRoles(HttpMethod.POST, userId, roles);
    }

    public void removeUserRealmRoles(String userId, List<KeycloakRole> roles) {
        changeUserRealmRoles(HttpMethod.DELETE, userId, roles);
    }

    private void changeUserRealmRoles(HttpMethod method, String userId, List<KeycloakRole> roles) {
        if (roles.isEmpty()) {
            return;
        }
        restClient.method(method)
                .uri(adminPath("/users/{id}/role-mappings/realm"), userId)
                .headers(this::adminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity();
    }

    private KeycloakTokenResponse requestToken(MultiValueMap<String, String> form, String errorMessage) {
        return restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                // Keycloak answers 401 for wrong credentials and 400 invalid_grant for a bad refresh token
                .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value()
                        || status.value() == HttpStatus.UNAUTHORIZED.value(), (request, response) -> {
                    log.warn("Keycloak rejected {} grant with status {}", form.getFirst("grant_type"),
                            response.getStatusCode().value());
                    throw new UnauthorizedException(errorMessage);
                })
                .body(KeycloakTokenResponse.class);
    }

    private MultiValueMap<String, String> clientForm(String grantType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        return form;
    }

    private void adminAuth(HttpHeaders headers) {
        headers.setBearerAuth(adminToken());
    }

    private synchronized String adminToken() {
        if (adminToken == null || Instant.now().isAfter(adminTokenExpiresAt)) {
            log.debug("Requesting a new Keycloak admin token");
            KeycloakTokenResponse token;
            try {
                token = requestToken(clientForm("client_credentials"), "Service account login failed");
            } catch (UnauthorizedException e) {
                // A misconfigured client is a server error, not the caller's fault
                throw new IllegalStateException("Main service client is not authorized in Keycloak", e);
            }
            adminToken = token.getAccessToken();
            adminTokenExpiresAt = Instant.now().plusSeconds(token.getExpiresIn() - TOKEN_EXPIRY_MARGIN_SECONDS);
        }
        return adminToken;
    }

    private String adminPath(String path) {
        return "/admin/realms/" + properties.realm() + path;
    }
}
