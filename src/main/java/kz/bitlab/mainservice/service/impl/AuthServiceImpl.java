package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.LoginRequest;
import kz.bitlab.mainservice.dto.RefreshTokenRequest;
import kz.bitlab.mainservice.dto.TokenResponse;
import kz.bitlab.mainservice.keycloak.KeycloakClient;
import kz.bitlab.mainservice.mapper.AuthMapper;
import kz.bitlab.mainservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakClient keycloakClient;
    private final AuthMapper authMapper;

    @Override
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt for user {}", request.getUsername());
        TokenResponse token = authMapper.toTokenResponse(
                keycloakClient.passwordGrant(request.getUsername(), request.getPassword()));
        log.info("User {} logged in", request.getUsername());
        return token;
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        log.info("Refreshing access token");
        return authMapper.toTokenResponse(keycloakClient.refreshTokenGrant(request.getRefreshToken()));
    }
}
