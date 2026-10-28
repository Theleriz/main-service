package kz.bitlab.mainservice.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection to Keycloak: base URL, realm and the confidential client of the main service.
 * The client is used both for password / refresh_token grants and, through its service account,
 * for the Keycloak Admin REST API (creating and updating users).
 */
@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(String url, String realm, String clientId, String clientSecret) {
}
