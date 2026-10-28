package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.LoginRequest;
import kz.bitlab.mainservice.dto.RefreshTokenRequest;
import kz.bitlab.mainservice.dto.TokenResponse;
import kz.bitlab.mainservice.exception.UnauthorizedException;
import kz.bitlab.mainservice.keycloak.KeycloakClient;
import kz.bitlab.mainservice.keycloak.dto.KeycloakTokenResponse;
import kz.bitlab.mainservice.mapper.AuthMapper;
import kz.bitlab.mainservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kz.bitlab.mainservice.TestData.ACCESS_TOKEN;
import static kz.bitlab.mainservice.TestData.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_validCredentials_returnsTokens() {
        KeycloakTokenResponse keycloakToken = TestData.keycloakTokenResponse();
        when(keycloakClient.passwordGrant("admin", "admin123")).thenReturn(keycloakToken);
        when(authMapper.toTokenResponse(keycloakToken)).thenReturn(TestData.tokenResponse());

        TokenResponse result = authService.login(new LoginRequest("admin", "admin123"));

        assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void login_invalidCredentials_throwsUnauthorized() {
        when(keycloakClient.passwordGrant("admin", "wrong"))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("admin", "wrong")));

        verify(authMapper, never()).toTokenResponse(any());
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        KeycloakTokenResponse keycloakToken = TestData.keycloakTokenResponse();
        when(keycloakClient.refreshTokenGrant(REFRESH_TOKEN)).thenReturn(keycloakToken);
        when(authMapper.toTokenResponse(keycloakToken)).thenReturn(TestData.tokenResponse());

        TokenResponse result = authService.refresh(new RefreshTokenRequest(REFRESH_TOKEN));

        assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
    }
}
